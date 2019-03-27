package org.openhab.binding.bluetooth.eqivablue.internal;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.openhab.binding.bluetooth.BluetoothCompletionStatus;
import org.openhab.binding.bluetooth.BluetoothDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceConnection {
    enum SendJobCommand {
        Send,
        Suspend,
        Stop
    }

    private final UUID UUID_EQIVA_BLUE_NOTIFICATION_CHARACTERISTIC = UUID
            .fromString("d0e8434d-cd29-0996-af41-6c90f4e0eb2a");
    private final UUID UUID_EQIVA_BLUE_CONTROL_CHARACTERISTIC = UUID.fromString("3fa4585a-ce4a-3bad-db4b-b8df8179ea09");
    private final long CONNECTION_KEEPUP_INTERVAL_IN_MILLISECONDS = 60000;
    private final long MESSAGE_SENT_CONFIRMATION_TIMEOUT_IN_MILLISECONDS = 10000;
    private final Logger logger = LoggerFactory.getLogger(DeviceConnection.class);

    private ThermostatContext context;
    private boolean shallKeepConnectionUp = false;
    private ScheduledExecutorService scheduler;
    private BluetoothDevice device;
    private BlockingQueue<EncodedSendMessage> sendQueue;
    private BlockingQueue<SendJobCommand> sendJobControlQueue;

    private BluetoothCharacteristic receivingCharacteristic = null;
    private BluetoothCharacteristic transmittingCharacteristic = null;
    private boolean notificationsEnabled = false;
    private Semaphore messageSent;
    private Future<?> sendJob;

    public void initialize(ThermostatContext theThermostatContext) {
        context = theThermostatContext;
        scheduler = context.getExecutorService();
        device = context.getBluetoothDevice();
        sendQueue = new LinkedBlockingQueue<EncodedSendMessage>();
        sendJobControlQueue = new LinkedBlockingQueue<SendJobCommand>();
        initializeCommunicationToDevice();
    }

    private void initializeCommunicationToDevice() {
        transmittingCharacteristic = device.getCharacteristic(UUID_EQIVA_BLUE_CONTROL_CHARACTERISTIC);
        receivingCharacteristic = device.getCharacteristic(UUID_EQIVA_BLUE_NOTIFICATION_CHARACTERISTIC);
        if (receivingCharacteristic != null) {
            notificationsEnabled = device.enableNotifications(receivingCharacteristic);
            logger.debug("Notifcations for {} {}", context.getName(),
                    (notificationsEnabled == true ? "enabled" : "disabled"));
        }
    }

    public void dispose() {
        if ((sendJob != null) && (sendJob.isDone() == false)) {
            sendJob.cancel(true);
            sendJob = null;
        }
        device.disableNotifications(receivingCharacteristic);
        device = null;
        receivingCharacteristic = null;
        transmittingCharacteristic = null;
        scheduler = null;
    }

    public void notifyServiceDiscovery() {
        if (canSendAndReceive() == false) {
            initializeCommunicationToDevice();
        }
    }

    private boolean canSendAndReceive() {
        return (transmittingCharacteristic != null) && (receivingCharacteristic != null) && notificationsEnabled;
    }

    public void startSending() {
        logger.debug("{} starts sending messages", context.getName());
        sendJobControlQueue.add(SendJobCommand.Send);
        if ((sendJob == null) || (sendJob.isDone())) {
            scheduleSendJob();
        }
    }

    public void suspendSending() {
        logger.debug("{} suspends sending messages", context.getName());
        sendJobControlQueue.add(SendJobCommand.Suspend);
    }

    public void stopSending() {
        logger.debug("{} stops sending messages", context.getName());
        sendJobControlQueue.add(SendJobCommand.Stop);
    }

    public void send(EncodedSendMessage messageToBeSent) {
        sendQueue.add(messageToBeSent);
    }

    public void notifySendCompletion(BluetoothCharacteristic characteristic, BluetoothCompletionStatus status) {
        messageSent.notify();
    }

    public void receiveMessageAndNotifyListener(BluetoothCharacteristic characteristic,
            ThermostatUpdateListener aListener) {
        if (characteristic.getUuid().equals(UUID_EQIVA_BLUE_NOTIFICATION_CHARACTERISTIC)) {
            int[] encodedMessage = characteristic.getValue();
            EncodedReceiveMessage message = new EncodedReceiveMessage(encodedMessage, aListener);
            message.decodeAndNotify();
        }
    }

    private void scheduleSendJob() {
        sendJob = scheduler.submit(() -> sendInLoop());
    }

    private void sendInLoop() {
        boolean keepSending = true;
        do {
            SendJobCommand command = sendJobControlQueue.poll();
            switch (command) {
                case Send:
                    processSendQueue();
                    sendJobControlQueue.add(SendJobCommand.Send);
                    break;
                case Stop:
                    keepSending = false;
                    break;
                case Suspend:
                default:
                    break;
            }
        } while (keepSending);
    }

    private void processSendQueue() {
        EncodedSendMessage messageToBeSent = null;
        messageToBeSent = pollNextMessageFromQueueWithTimeout();
        if (messageToBeSent != null) {
            keepSendingUntilConfirmationReceived(messageToBeSent);
        } else if (shallKeepConnectionUp) {
            sendQueryMessageToKeepConnectionUp();
        }
    }

    private EncodedSendMessage pollNextMessageFromQueueWithTimeout() {
        EncodedSendMessage message;
        try {
            message = sendQueue.poll(CONNECTION_KEEPUP_INTERVAL_IN_MILLISECONDS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            message = null;
        }
        return message;
    }

    private void keepSendingUntilConfirmationReceived(EncodedSendMessage messageToBeSent) {
        do {
            sendMessage(messageToBeSent);
        } while (messageIsSent() == false);
    }

    private void sendQueryMessageToKeepConnectionUp() {
        send(EncodedSendMessage.queryStatus());
    }

    private void sendMessage(EncodedSendMessage messageToBeSent) {
        if (canSendAndReceive()) {
            device.writeCharacteristic(transmittingCharacteristic);
        }
    }

    private boolean messageIsSent() {
        boolean messageIsSent = false;
        try {
            messageIsSent = messageSent.tryAcquire(MESSAGE_SENT_CONFIRMATION_TIMEOUT_IN_MILLISECONDS,
                    TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
        }
        return messageIsSent;
    }

}

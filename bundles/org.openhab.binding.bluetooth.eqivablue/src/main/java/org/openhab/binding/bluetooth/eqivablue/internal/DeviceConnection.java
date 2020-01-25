/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.bluetooth.eqivablue.internal;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.openhab.binding.bluetooth.BluetoothCompletionStatus;
import org.openhab.binding.bluetooth.BluetoothDescriptor;
import org.openhab.binding.bluetooth.BluetoothDevice;
import org.openhab.binding.bluetooth.BluetoothDeviceListener;
import org.openhab.binding.bluetooth.eqivablue.internal.messages.SendMessage;
import org.openhab.binding.bluetooth.eqivablue.internal.messages.SendMessage.MessageStatus;
import org.openhab.binding.bluetooth.notification.BluetoothConnectionStatusNotification;
import org.openhab.binding.bluetooth.notification.BluetoothScanNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Frank Heister - Initial contribution
 */
public class DeviceConnection implements BluetoothDeviceListener {

    private static final UUID UUID_EQIVA_BLUE_NOTIFICATION_CHARACTERISTIC = UUID
            .fromString("d0e8434d-cd29-0996-af41-6c90f4e0eb2a");
    private static final UUID UUID_EQIVA_BLUE_CONTROL_CHARACTERISTIC = UUID
            .fromString("3fa4585a-ce4a-3bad-db4b-b8df8179ea09");
    private final Logger logger = LoggerFactory.getLogger(DeviceConnection.class);
    private BluetoothDevice bluetoothDevice;
    private BluetoothCharacteristic receivingCharacteristic = null;
    private BluetoothCharacteristic transmittingCharacteristic = null;
    private boolean notificationsEnabled = false;
    private ThermostatContext thermostatContext;
    private ThermostatUpdateListener updateListener;
    private Map<Type, SendMessage> messagesToBeSent;
    private SendMessage currentlyProcessedMessage = null;

    public DeviceConnection(BluetoothDevice theBluetoothDevice, ThermostatUpdateListener theUpdateListener,
            ThermostatContext theThermostatContext) {
        bluetoothDevice = theBluetoothDevice;
        updateListener = theUpdateListener;
        thermostatContext = theThermostatContext;
        messagesToBeSent = Collections.synchronizedMap(new LinkedHashMap<Type, SendMessage>());
    }

    public void dispose() {
        bluetoothDevice.disableNotifications(receivingCharacteristic);
        receivingCharacteristic = null;
        transmittingCharacteristic = null;
        messagesToBeSent.clear();
        messagesToBeSent = null;
    }

    public void sendMessage(SendMessage theMessage) {
        logger.debug("SendMessage called for message {} on device {}", theMessage, bluetoothDevice.getAddress());
        addMessageToQueue(theMessage);
        connectToStartSending();
    }

    private void addMessageToQueue(SendMessage theMessage) {
        messagesToBeSent.put(theMessage.getClass(), theMessage);
    }

    private void connectToStartSending() {
        logger.debug("Device {} is in state {}", bluetoothDevice.getAddress(), bluetoothDevice.getConnectionState());
        switch (bluetoothDevice.getConnectionState()) {
            case DISCOVERING:
            case DISCOVERED:
            case DISCONNECTING:
            case DISCONNECTED:
                logger.debug("Device {} needs to be connected for sending messages", bluetoothDevice.getAddress());
                if (!bluetoothDevice.connect()) {
                    logger.debug("... but connection not possible");
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onConnectionStateChange(@NonNull BluetoothConnectionStatusNotification connectionNotification) {
        logger.debug("Connection status of {} / {} has changed to {}", thermostatContext.getName(),
                bluetoothDevice.getAddress(), connectionNotification.getConnectionState());
        switch (connectionNotification.getConnectionState()) {
            case DISCOVERED:
                if (!bluetoothDevice.connect()) {
                    logger.debug("Error connecting to device after discovery.");
                }
                break;
            case CONNECTED:
                // updateListener.updateThingStatus(ThingStatus.ONLINE);
                if (!canSendAndReceive()) {
                    if (!bluetoothDevice.discoverServices()) {
                        logger.debug("Error while discovering services");
                    }
                } else {
                    thermostatContext.startSendJob(() -> sendAllQueuedMessagesAndThenDisconnect());
                }
                break;
            case DISCONNECTED:
                // updateListener.updateThingStatus(ThingStatus.OFFLINE);
                break;
            default:
                break;
        }
    }

    private void sendAllQueuedMessagesAndThenDisconnect() {
        while (!messagesToBeSent.isEmpty()) {
            synchronized (messagesToBeSent) {
                Iterator<Entry<Type, SendMessage>> iterator = messagesToBeSent.entrySet().iterator();
                currentlyProcessedMessage = iterator.next().getValue();
                iterator.remove();
            }
            keepSendingUntilConfirmationReceived(currentlyProcessedMessage);
            waitForResponse(currentlyProcessedMessage);
            currentlyProcessedMessage = null;
        }
        bluetoothDevice.disconnect();
    }

    private void keepSendingUntilConfirmationReceived(SendMessage messageToBeSent) {
        do {
            writeMessageContentToCharacteristic(messageToBeSent);
            messageToBeSent.blockOrTimeOut(thermostatContext.getMessageSentConfirmationTimeoutInMilliseconds());
        } while (!messageToBeSent.hasStatus(MessageStatus.SENDING_CONFIRMED));
    }

    private void writeMessageContentToCharacteristic(SendMessage messageToBeSent) throws UnsupportedOperationException {
        if (canSendAndReceive()) {
            messageToBeSent.setStatus(MessageStatus.SENT);
            int[] encodedContext = messageToBeSent.getEncodedContent();
            transmittingCharacteristic.setValue(encodedContext);
            bluetoothDevice.writeCharacteristic(transmittingCharacteristic);
            logger.debug("{} sent command {}", thermostatContext.getName(), encodedContext);
        } else {
            throw new UnsupportedOperationException(
                    "Connection not able to send and receive. Probably not initialized.");
        }
    }

    private void waitForResponse(SendMessage message) {
        do {
            logger.debug("Waiting for repsonse: {}", bluetoothDevice.getAddress());
            message.blockOrTimeOut(thermostatContext.getMessageResponseReceivedTimeoutInMilliseconds());
        } while (!message.hasStatus(MessageStatus.RESPONSE_RECEIVED));
    }

    @Override
    public void onServicesDiscovered() {
        logger.debug("Services of {} discovered", bluetoothDevice.getAddress());
        if (!canSendAndReceive()) {
            retrieveCharacteristics();
        }
        thermostatContext.startSendJob(() -> sendAllQueuedMessagesAndThenDisconnect());
    }

    private void retrieveCharacteristics() {
        transmittingCharacteristic = bluetoothDevice.getCharacteristic(UUID_EQIVA_BLUE_CONTROL_CHARACTERISTIC);
        receivingCharacteristic = bluetoothDevice.getCharacteristic(UUID_EQIVA_BLUE_NOTIFICATION_CHARACTERISTIC);
        if (receivingCharacteristic != null) {
            notificationsEnabled = bluetoothDevice.enableNotifications(receivingCharacteristic);
            logger.debug("Notifcations for {} {}", thermostatContext.getName(),
                    (notificationsEnabled ? "enabled" : "disabled"));
        }
    }

    private boolean canSendAndReceive() {
        return (transmittingCharacteristic != null) && (receivingCharacteristic != null) /* && notificationsEnabled */;
    }

    @Override
    public void onCharacteristicWriteComplete(@NonNull BluetoothCharacteristic characteristic,
            @NonNull BluetoothCompletionStatus status) {
        if (isControlCharacteristic(characteristic) && (status == BluetoothCompletionStatus.SUCCESS)) {
            logger.debug("Characteristic written for {}", bluetoothDevice.getAddress());
            currentlyProcessedMessage.setStatus(MessageStatus.SENDING_CONFIRMED);
        }
    }

    private boolean isControlCharacteristic(BluetoothCharacteristic characteristic) {
        return (characteristic.getUuid().equals(UUID_EQIVA_BLUE_CONTROL_CHARACTERISTIC));
    }

    @Override
    public void onCharacteristicUpdate(@NonNull BluetoothCharacteristic characteristic) {
        if (isNotificationCharacteristic(characteristic)) {
            logger.debug("Characteristics update received for {}", bluetoothDevice.getAddress());
            currentlyProcessedMessage.setStatus(MessageStatus.RESPONSE_RECEIVED);
            receiveMessageAndNotifyListener(characteristic);
        }
    }

    private boolean isNotificationCharacteristic(BluetoothCharacteristic characteristic) {
        return (characteristic.getUuid().equals(UUID_EQIVA_BLUE_NOTIFICATION_CHARACTERISTIC));
    }

    private void receiveMessageAndNotifyListener(BluetoothCharacteristic characteristic) {
        int[] encodedMessage = characteristic.getValue();
        EncodedReceiveMessage message = new EncodedReceiveMessage(encodedMessage, updateListener);
        message.decodeAndNotify();
    }

    @Override
    public void onScanRecordReceived(@NonNull BluetoothScanNotification scanNotification) {
    }

    @Override
    public void onCharacteristicReadComplete(@NonNull BluetoothCharacteristic characteristic,
            @NonNull BluetoothCompletionStatus status) {
    }

    @Override
    public void onDescriptorUpdate(@NonNull BluetoothDescriptor bluetoothDescriptor) {
    }

}

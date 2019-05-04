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

import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.openhab.binding.bluetooth.BluetoothCompletionStatus;
import org.openhab.binding.bluetooth.BluetoothDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Frank Heister - Initial contribution
 */
public class DeviceConnection {

    private static final UUID UUID_EQIVA_BLUE_NOTIFICATION_CHARACTERISTIC = UUID
            .fromString("d0e8434d-cd29-0996-af41-6c90f4e0eb2a");
    private static final UUID UUID_EQIVA_BLUE_CONTROL_CHARACTERISTIC = UUID
            .fromString("3fa4585a-ce4a-3bad-db4b-b8df8179ea09");
    private final Logger logger = LoggerFactory.getLogger(DeviceConnection.class);
    private BluetoothDevice bluetoothDevice;
    private BluetoothCharacteristic receivingCharacteristic = null;
    private BluetoothCharacteristic transmittingCharacteristic = null;
    private boolean notificationsEnabled = false;
    private Semaphore messageSentSignal;
    private ThermostatContext thermostatContext;

    public DeviceConnection(BluetoothDevice theBluetoothDevice, ThermostatContext theThermostatContext) {
        bluetoothDevice = theBluetoothDevice;
        thermostatContext = theThermostatContext;
    }

    private void enableCommunicationToDevice() {
        transmittingCharacteristic = bluetoothDevice.getCharacteristic(UUID_EQIVA_BLUE_CONTROL_CHARACTERISTIC);
        receivingCharacteristic = bluetoothDevice.getCharacteristic(UUID_EQIVA_BLUE_NOTIFICATION_CHARACTERISTIC);
        if (receivingCharacteristic != null) {
            notificationsEnabled = bluetoothDevice.enableNotifications(receivingCharacteristic);
            logger.debug("Notifcations for {} {}", thermostatContext.getName(),
                    (notificationsEnabled == true ? "enabled" : "disabled"));
        }
    }

    public void disableCommunicationToDevice() {
        bluetoothDevice.disableNotifications(receivingCharacteristic);
        receivingCharacteristic = null;
        transmittingCharacteristic = null;
    }

    public void notifyServiceDiscovery() {
        if (canSendAndReceive() == false) {
            enableCommunicationToDevice();
        }
    }

    public void sendMessage(EncodedSendMessage messageToBeSent) {
        keepSendingUntilConfirmationReceived(messageToBeSent);
    }

    public void notifySendCompletion(BluetoothCharacteristic characteristic, BluetoothCompletionStatus status) {
        if (isCharacteristicOfInterest(characteristic) && (status == BluetoothCompletionStatus.SUCCESS)) {
            messageSentSignal.notify();
        }
    }

    public void receiveMessageAndNotifyListener(BluetoothCharacteristic characteristic,
            ThermostatUpdateListener aListener) {
        if (isCharacteristicOfInterest(characteristic)) {
            int[] encodedMessage = characteristic.getValue();
            EncodedReceiveMessage message = new EncodedReceiveMessage(encodedMessage, aListener);
            message.decodeAndNotify();
        }
    }

    private boolean canSendAndReceive() {
        return (transmittingCharacteristic != null) && (receivingCharacteristic != null) && notificationsEnabled;
    }

    private void keepSendingUntilConfirmationReceived(EncodedSendMessage messageToBeSent) {
        do {
            writeMessageContentToCharacteristic(messageToBeSent);
        } while (messageIsSent() == false);
    }

    private void writeMessageContentToCharacteristic(EncodedSendMessage messageToBeSent)
            throws UnsupportedOperationException {
        if (canSendAndReceive()) {
            int[] encodedContext = messageToBeSent.getEncodedContent();
            transmittingCharacteristic.setValue(encodedContext);
            bluetoothDevice.writeCharacteristic(transmittingCharacteristic);
            logger.debug("{} sent command {}", thermostatContext.getName(), encodedContext);
        } else {
            throw new UnsupportedOperationException(
                    "Connection not able to send and receive. Probably not initialized.");
        }
    }

    private boolean messageIsSent() {
        boolean messageIsSent = false;
        try {
            messageIsSent = messageSentSignal.tryAcquire(
                    thermostatContext.getMessageSentConfirmationTimeoutInMilliseconds(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
        }
        return messageIsSent;
    }

    private boolean isCharacteristicOfInterest(BluetoothCharacteristic characteristic) {
        return (characteristic.getUuid().equals(UUID_EQIVA_BLUE_NOTIFICATION_CHARACTERISTIC));
    }

}

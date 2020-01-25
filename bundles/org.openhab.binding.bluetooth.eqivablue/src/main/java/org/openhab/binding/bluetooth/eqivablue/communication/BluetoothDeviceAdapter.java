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
package org.openhab.binding.bluetooth.eqivablue.communication;

import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.openhab.binding.bluetooth.BluetoothCompletionStatus;
import org.openhab.binding.bluetooth.BluetoothDescriptor;
import org.openhab.binding.bluetooth.BluetoothDevice;
import org.openhab.binding.bluetooth.BluetoothDeviceListener;
import org.openhab.binding.bluetooth.eqivablue.communication.states.DeviceHandler;
import org.openhab.binding.bluetooth.eqivablue.internal.messages.SendMessage;
import org.openhab.binding.bluetooth.notification.BluetoothConnectionStatusNotification;
import org.openhab.binding.bluetooth.notification.BluetoothScanNotification;

/**
 * @author Frank Heister - Initial contribution
 */
@NonNullByDefault
public class BluetoothDeviceAdapter implements BluetoothDeviceListener {

    private static final UUID UUID_EQIVA_BLUE_CONTROL_CHARACTERISTIC = UUID
            .fromString("3fa4585a-ce4a-3bad-db4b-b8df8179ea09");
    private static final UUID UUID_EQIVA_BLUE_NOTIFICATION_CHARACTERISTIC = UUID
            .fromString("d0e8434d-cd29-0996-af41-6c90f4e0eb2a");

    private BluetoothDevice device;
    private DeviceHandler deviceHandler;

    @Nullable
    private BluetoothCharacteristic controlCharacteristic;

    @Nullable
    private BluetoothCharacteristic notificationCharacteristic;

    public BluetoothDeviceAdapter(BluetoothDevice theBluetoothDevice, DeviceHandler theDeviceHandler) {
        device = theBluetoothDevice;
        deviceHandler = theDeviceHandler;
    }

    @Override
    public void onScanRecordReceived(BluetoothScanNotification scanNotification) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onConnectionStateChange(BluetoothConnectionStatusNotification connectionNotification) {
        switch (connectionNotification.getConnectionState()) {
            case CONNECTED:
                deviceHandler.notifyConnectionEstablished();
                break;
            default:
                break;
        }
    }

    @Override
    public void onServicesDiscovered() {
    }

    @Override
    public void onCharacteristicReadComplete(BluetoothCharacteristic characteristic, BluetoothCompletionStatus status) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onCharacteristicWriteComplete(BluetoothCharacteristic characteristic,
            BluetoothCompletionStatus status) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onCharacteristicUpdate(BluetoothCharacteristic characteristic) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onDescriptorUpdate(BluetoothDescriptor bluetoothDescriptor) {
        // TODO Auto-generated method stub
    }

    public boolean requestConnection() {
        return device.connect();
    }

    public boolean requestDiscoverServices() {
        return device.discoverServices();
    }

    public void requestCharacteristics() {
    }

    public boolean requestDisconnect() {
        return device.disconnect();
    }

    public boolean getCharacteristics() {
        controlCharacteristic = device.getCharacteristic(UUID_EQIVA_BLUE_CONTROL_CHARACTERISTIC);
        notificationCharacteristic = device.getCharacteristic(UUID_EQIVA_BLUE_NOTIFICATION_CHARACTERISTIC);
        return (controlCharacteristic != null) && (notificationCharacteristic != null);
    }

    public boolean characteristicsAreAvailable() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean writeCharacteristic(SendMessage theMessage) {
        if (controlCharacteristic != null) {
            controlCharacteristic.setValue(theMessage.getEncodedContent());
            return device.writeCharacteristic(controlCharacteristic);
        } else {
            return false;
        }
    }
}

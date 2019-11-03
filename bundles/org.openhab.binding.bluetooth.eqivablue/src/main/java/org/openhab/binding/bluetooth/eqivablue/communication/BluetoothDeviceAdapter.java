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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.openhab.binding.bluetooth.BluetoothCompletionStatus;
import org.openhab.binding.bluetooth.BluetoothDescriptor;
import org.openhab.binding.bluetooth.BluetoothDevice;
import org.openhab.binding.bluetooth.BluetoothDeviceListener;
import org.openhab.binding.bluetooth.notification.BluetoothConnectionStatusNotification;
import org.openhab.binding.bluetooth.notification.BluetoothScanNotification;

/**
 * @author Frank Heister - Initial contribution
 */
@NonNullByDefault
public class BluetoothDeviceAdapter implements BluetoothDeviceListener {

    private BluetoothDevice device;
    private DeviceHandler deviceHandler;

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
        // TODO Auto-generated method stub

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

}

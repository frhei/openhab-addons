/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.eqivablue.internal.communication;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothAdapter;
import org.openhab.binding.bluetooth.BluetoothAddress;
import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.openhab.binding.bluetooth.BluetoothCompletionStatus;
import org.openhab.binding.bluetooth.BluetoothDescriptor;
import org.openhab.binding.bluetooth.BluetoothDevice;
import org.openhab.binding.bluetooth.BluetoothDeviceListener;
import org.openhab.binding.bluetooth.BluetoothService;
import org.openhab.binding.bluetooth.eqivablue.internal.messages.EncodedReceiveMessage;
import org.openhab.binding.bluetooth.eqivablue.internal.messages.SendMessage;
import org.openhab.binding.bluetooth.notification.BluetoothConnectionStatusNotification;
import org.openhab.binding.bluetooth.notification.BluetoothScanNotification;

/**
 * @author Frank Heister - Initial contribution
 */
@NonNullByDefault
public class EqivablueDeviceAdapter implements BluetoothDeviceListener {

    private static final UUID UUID_EQIVA_BLUE_SERVICE = UUID.fromString("3e135142-654f-9090-134a-a6ff5bb77046");

    private static final UUID UUID_EQIVA_BLUE_CONTROL_CHARACTERISTIC = UUID
            .fromString("3fa4585a-ce4a-3bad-db4b-b8df8179ea09");
    private static final UUID UUID_EQIVA_BLUE_NOTIFICATION_CHARACTERISTIC = UUID
            .fromString("d0e8434d-cd29-0996-af41-6c90f4e0eb2a");

    private Set<EqivablueDeviceListener> deviceListeners;
    private BluetoothDevice device;

    @Nullable
    private BluetoothCharacteristic controlCharacteristic;

    @Nullable
    private BluetoothCharacteristic notificationCharacteristic;

    public EqivablueDeviceAdapter(BluetoothDevice theDevice) {
        device = theDevice;
        device.addListener(this);
        deviceListeners = new HashSet<EqivablueDeviceListener>();
    }

    public void dispose() {
        device.removeListener(this);
        deviceListeners.clear();
    }

    public boolean addListener(EqivablueDeviceListener theListener) {
        return deviceListeners.add(theListener);
    }

    public boolean removeListener(EqivablueDeviceListener theListener) {
        return deviceListeners.remove(theListener);
    }

    @Override
    public void onScanRecordReceived(BluetoothScanNotification scanNotification) {
        deviceListeners.forEach((listener) -> {
            listener.notifyReceivedSignalStrength(scanNotification.getRssi());
        });
    }

    @Override
    public void onConnectionStateChange(BluetoothConnectionStatusNotification connectionNotification) {
        switch (connectionNotification.getConnectionState()) {
            case CONNECTED:
                deviceListeners.forEach((listener) -> {
                    listener.notifyConnectionEstablished();
                });
                break;
            case DISCONNECTED:
                deviceListeners.forEach((listener) -> {
                    listener.notifyConnectionClosed();
                });
                break;
            default:
                break;
        }
    }

    @Override
    public void onServicesDiscovered() {
        BluetoothService service = device.getServices(UUID_EQIVA_BLUE_SERVICE);
        if ((service != null) && (service.getHandleStart() != 0) && (service.getHandleEnd() != 0)) {
            deviceListeners.forEach((listener) -> {
                listener.notifyServicesDiscovered();
            });
        }
    }

    @Override
    public void onCharacteristicReadComplete(BluetoothCharacteristic characteristic, BluetoothCompletionStatus status) {
    }

    @Override
    public void onCharacteristicWriteComplete(BluetoothCharacteristic characteristic,
            BluetoothCompletionStatus status) {
        if ((characteristic == controlCharacteristic) && (status == BluetoothCompletionStatus.SUCCESS)) {
            deviceListeners.forEach((listener) -> {
                listener.notifyCharacteristicWritten();
            });
        }
    }

    @Override
    public void onCharacteristicUpdate(BluetoothCharacteristic characteristic) {
        if (characteristic == notificationCharacteristic) {
            EncodedReceiveMessage message = new EncodedReceiveMessage(characteristic.getValue());
            deviceListeners.forEach((listener) -> {
                listener.notifyCharacteristicUpdate(message);
            });
        }
    }

    @Override
    public void onDescriptorUpdate(BluetoothDescriptor bluetoothDescriptor) {
    }

    public void requestScan() {
        device.getAdapter().scanStart();
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
        BluetoothCharacteristic localCharacteristic = controlCharacteristic;
        if (localCharacteristic != null) {
            localCharacteristic.setValue(theMessage.getEncodedContent());
            return device.writeCharacteristic(localCharacteristic);
        } else {
            return false;
        }
    }

    public BluetoothAddress getAddress() {
        return device.getAddress();
    }

    @Override
    public void onAdapterChanged(BluetoothAdapter adapter) {
        // TODO Auto-generated method stub

    }
}

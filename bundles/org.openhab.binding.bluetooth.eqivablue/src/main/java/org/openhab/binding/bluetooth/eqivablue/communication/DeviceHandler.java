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

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Frank Heister - Initial contribution
 */
@NonNullByDefault
public class DeviceHandler {

    private BluetoothDeviceAdapter deviceAdapter;
    private DeviceContext context;
    private Map<Type, DeviceState> states;
    private DeviceState currentState;

    public DeviceHandler(BluetoothDeviceAdapter theDeviceAdapter, DeviceContext theContext) {
        deviceAdapter = theDeviceAdapter;
        context = theContext;
        states = new HashMap<Type, DeviceState>();
        states.put(NoSignalState.class, new NoSignalState(this));
        states.put(ConnectingForServiceDiscoveryState.class, new ConnectingForServiceDiscoveryState(this));
        states.put(DiscoveringServicesState.class, new DiscoveringServicesState(this));
        states.put(FailureState.class, new FailureState(this));

        currentState = states.get(NoSignalState.class);
    }

    void setState(Type type) {
        currentState.onExit();
        currentState = states.get(type);
        currentState.onEntry();
    }

    BluetoothDeviceAdapter getDeviceAdapter() {
        return deviceAdapter;
    }

    public DeviceStatus getStatus() {
        return currentState.getStatus();
    }

    public void updateReceivedSignalStrength(int rssi) {
        if (rssi >= context.getMinimalSignalStrengthForAcceptingCommunicationToDevice()) {
            currentState.indicateReceivedSignalStrength(rssi);
        } else {
            currentState.indicateSignalLoss();
        }
    }

    public void notifyConnectionEstablished() {
        currentState.notifyConnectionEstablished();
    }

    public void notifyConnectionClosed() {
        currentState.notifyConnectionClosed();
    }

    public boolean requestConnection() {
        return deviceAdapter.requestConnection();
    }

    public boolean requestDiscoverServices() {
        return deviceAdapter.requestDiscoverServices();
    }

    public DeviceContext getContext() {
        return context;
    }

    public boolean requestDisconnect() {
        return deviceAdapter.requestDisconnect();
    }

}

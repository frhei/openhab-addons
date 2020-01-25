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
package org.openhab.binding.bluetooth.eqivablue.communication.states;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.openhab.binding.bluetooth.eqivablue.communication.BluetoothDeviceAdapter;
import org.openhab.binding.bluetooth.eqivablue.communication.CommandHandler;
import org.openhab.binding.bluetooth.eqivablue.internal.EncodedReceiveMessage;
import org.openhab.binding.bluetooth.eqivablue.internal.ThermostatUpdateListener;
import org.openhab.binding.bluetooth.eqivablue.internal.messages.SendMessage;

/**
 * @author Frank Heister - Initial contribution
 */
@NonNullByDefault
public class DeviceHandler {

    private BluetoothDeviceAdapter deviceAdapter;
    private CommandHandler commandHandler;
    private ThermostatUpdateListener updateListener;
    private DeviceContext context;
    private Map<Type, DeviceState> states;
    private DeviceState currentState;

    // private final Logger logger = LoggerFactory.getLogger(DeviceHandler.class);

    public DeviceHandler(BluetoothDeviceAdapter theDeviceAdapter, CommandHandler theCommandHandler,
            ThermostatUpdateListener theUpdateListener, DeviceContext theContext) {
        deviceAdapter = theDeviceAdapter;
        commandHandler = theCommandHandler;
        updateListener = theUpdateListener;
        context = theContext;
        states = new HashMap<Type, DeviceState>();
        states.put(NoSignalState.class, new NoSignalState(this));
        states.put(ConnectingForServiceDiscoveryState.class, new ConnectingForServiceDiscoveryState(this));
        states.put(DiscoveringServicesState.class, new DiscoveringServicesState(this));
        states.put(RetrievingCharacteristicsState.class, new RetrievingCharacteristicsState(this));
        states.put(WaitingForDisconnectState.class, new WaitingForDisconnectState(this));
        states.put(TransmittingMessageState.class, new TransmittingMessageState(this));
        states.put(IdleState.class, new IdleState(this));
        states.put(ConnectingForCommandProcessingState.class, new ConnectingForCommandProcessingState(this));
        states.put(WaitingForResponseState.class, new WaitingForResponseState(this));
        states.put(FailureState.class, new FailureState(this));

        currentState = states.get(NoSignalState.class);
        updateListener.updateThingStatus(currentState.getStatus());
    }

    @Trace
    public void setState(Type type) {
        // logger.debug("{} -> {}", currentState.getClass().getTypeName(), type.getTypeName());
        ThingStatus oldStatus, newStatus;
        oldStatus = currentState.getStatus();
        currentState.onExit();
        currentState = states.get(type);
        currentState.onEntry();
        newStatus = currentState.getStatus();
        if (oldStatus != newStatus) {
            updateListener.updateThingStatus(newStatus);
        }
    }

    BluetoothDeviceAdapter getDeviceAdapter() {
        return deviceAdapter;
    }

    public ThingStatus getStatus() {
        return currentState.getStatus();
    }

    public void updateReceivedSignalStrength(int rssi) {
        // logger.debug("RSSI {}", rssi);
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

    public CommandHandler getCommandHandler() {
        return commandHandler;
    }

    public boolean requestDisconnect() {
        return deviceAdapter.requestDisconnect();
    }

    public void notifyServicesDiscovered() {
        currentState.notifyServicesDiscovered();
    }

    public boolean getCharacteristics() {
        return deviceAdapter.getCharacteristics();
    }

    public boolean characteristicsAreAvailable() {
        return deviceAdapter.characteristicsAreAvailable();
    }

    public void notifyCommandProcessingRequest() {
        currentState.notifyCommandProcessingRequest();
    }

    public boolean transmitMessage(SendMessage message) {
        return deviceAdapter.writeCharacteristic(message);
    }

    public void notifyCharacteristicWritten() {
        currentState.notifyMessageTransmitted();
    }

    public void notifyCharacteristicUpdate(EncodedReceiveMessage message) {
        currentState.notifyCharacteristicUpdate(message);
    }
}

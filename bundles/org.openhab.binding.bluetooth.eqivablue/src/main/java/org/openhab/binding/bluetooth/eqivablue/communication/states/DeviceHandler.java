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
package org.openhab.binding.bluetooth.eqivablue.communication.states;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.openhab.binding.bluetooth.eqivablue.communication.CommandHandler;
import org.openhab.binding.bluetooth.eqivablue.communication.EqivablueDeviceAdapter;
import org.openhab.binding.bluetooth.eqivablue.communication.EqivablueDeviceListener;
import org.openhab.binding.bluetooth.eqivablue.internal.EncodedReceiveMessage;
import org.openhab.binding.bluetooth.eqivablue.internal.ThermostatUpdateListener;
import org.openhab.binding.bluetooth.eqivablue.internal.messages.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Frank Heister - Initial contribution
 */
@NonNullByDefault
public class DeviceHandler implements EqivablueDeviceListener {

    private final Logger logger = LoggerFactory.getLogger(DeviceHandler.class);

    private EqivablueDeviceAdapter deviceAdapter;
    private CommandHandler commandHandler;
    private ThermostatUpdateListener updateListener;
    private DeviceContext context;
    private Map<Type, DeviceState> states;
    private DeviceState currentState;

    // private final Logger logger = LoggerFactory.getLogger(DeviceHandler.class);

    public DeviceHandler(EqivablueDeviceAdapter theDeviceAdapter, CommandHandler theCommandHandler,
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

        deviceAdapter.addListener(this);
    }

    public void dispose() {
        deviceAdapter.removeListener(this);
    }

    public void setState(Type type) {
        logger.debug("{}: {} -> {}", deviceAdapter.getAddress(), currentState.getClass().getTypeName(),
                type.getTypeName());
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

    EqivablueDeviceAdapter getDeviceAdapter() {
        return deviceAdapter;
    }

    public ThingStatus getStatus() {
        return currentState.getStatus();
    }

    @Override
    public void notifyReceivedSignalStrength(int rssi) {
        logger.debug("{}: RSSI {}", deviceAdapter.getAddress(), rssi);
        if (rssi >= context.getMinimalSignalStrengthForAcceptingCommunicationToDevice()) {
            currentState.indicateReceivedSignalStrength(rssi);
        } else {
            currentState.indicateSignalLoss();
        }
    }

    @Override
    public void notifyConnectionEstablished() {
        logger.debug("{}: notifyConnectionEstablished", deviceAdapter.getAddress());
        currentState.notifyConnectionEstablished();
    }

    @Override
    public void notifyConnectionClosed() {
        logger.debug("{}: notifyConnectionClosed", deviceAdapter.getAddress());
        currentState.notifyConnectionClosed();
    }

    public boolean requestConnection() {
        logger.debug("{}: requestConnection", deviceAdapter.getAddress());
        return deviceAdapter.requestConnection();
    }

    public boolean requestDiscoverServices() {
        logger.debug("{}: requestDiscoverServices", deviceAdapter.getAddress());
        return deviceAdapter.requestDiscoverServices();
    }

    public DeviceContext getContext() {
        return context;
    }

    public CommandHandler getCommandHandler() {
        return commandHandler;
    }

    public boolean requestDisconnect() {
        logger.debug("{}: requestDisconnect", deviceAdapter.getAddress());
        return deviceAdapter.requestDisconnect();
    }

    @Override
    public void notifyServicesDiscovered() {
        logger.debug("{}: notifyServicesDiscovered", deviceAdapter.getAddress());
        currentState.notifyServicesDiscovered();
    }

    public boolean getCharacteristics() {
        logger.debug("{}: getCharacteristics", deviceAdapter.getAddress());
        return deviceAdapter.getCharacteristics();
    }

    public boolean characteristicsAreAvailable() {
        logger.debug("{}: characteristicsAreAvailable", deviceAdapter.getAddress());
        return deviceAdapter.characteristicsAreAvailable();
    }

    public void notifyCommandProcessingRequest() {
        logger.debug("{}: notifyCommandProcessingRequest", deviceAdapter.getAddress());
        currentState.notifyCommandProcessingRequest();
    }

    public boolean transmitMessage(SendMessage message) {
        logger.debug("{}: transmitMessage {}", deviceAdapter.getAddress(), message);
        return deviceAdapter.writeCharacteristic(message);
    }

    @Override
    public void notifyCharacteristicWritten() {
        logger.debug("{}: notifyCharacteristicWritten", deviceAdapter.getAddress());
        currentState.notifyMessageTransmitted();
    }

    @Override
    public void notifyCharacteristicUpdate(EncodedReceiveMessage message) {
        logger.debug("{}: notifyCharacteristicUpdate {}", deviceAdapter.getAddress(), message);
        currentState.notifyCharacteristicUpdate(message);
    }

    public void handleMessage(EncodedReceiveMessage message) {
        logger.debug("{}: handleMessage {}", deviceAdapter.getAddress(), message);
        message.decodeAndNotify(updateListener);
    }
}

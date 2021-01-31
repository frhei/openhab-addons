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
package org.openhab.binding.bluetooth.eqivablue.internal.communication.states;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingStatus;
import org.openhab.binding.bluetooth.eqivablue.handler.ThermostatUpdateListener;
import org.openhab.binding.bluetooth.eqivablue.internal.communication.CommandHandler;
import org.openhab.binding.bluetooth.eqivablue.internal.communication.EqivablueDeviceAdapter;
import org.openhab.binding.bluetooth.eqivablue.internal.communication.EqivablueDeviceListener;
import org.openhab.binding.bluetooth.eqivablue.internal.messages.EncodedReceiveMessage;
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
        currentState = new NoSignalState(this);
        states.put(NoSignalState.class, currentState);
        states.put(ConnectingForServiceDiscoveryState.class, new ConnectingForServiceDiscoveryState(this));
        states.put(DiscoveringServicesState.class, new DiscoveringServicesState(this));
        states.put(RetrievingCharacteristicsState.class, new RetrievingCharacteristicsState(this));
        states.put(WaitingForDisconnectState.class, new WaitingForDisconnectState(this));
        states.put(TransmittingMessageState.class, new TransmittingMessageState(this));
        states.put(IdleState.class, new IdleState(this));
        states.put(ConnectingForCommandProcessingState.class, new ConnectingForCommandProcessingState(this));
        states.put(WaitingForResponseState.class, new WaitingForResponseState(this));
        states.put(FailureState.class, new FailureState(this));

        updateListener.updateThingStatus(currentState.getStatus());

        deviceAdapter.addListener(this);
    }

    public void dispose() {
        deviceAdapter.removeListener(this);
    }

    public synchronized void setState(Type type) {
        var newState = states.get(type);
        if (newState == null)
            throw new IllegalStateException();

        logger.debug("{}: {} -> {}", deviceAdapter.getAddress(), currentState.getClass().getSimpleName(),
                newState.getClass().getSimpleName());
        ThingStatus oldStatus, newStatus;
        oldStatus = currentState.getStatus();
        currentState.onExit();
        currentState = newState;
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

    public boolean getCharacteristics() {
        logger.debug("{}: getCharacteristics", deviceAdapter.getAddress());
        return deviceAdapter.getCharacteristics();
    }

    public boolean characteristicsAreAvailable() {
        logger.debug("{}: characteristicsAreAvailable", deviceAdapter.getAddress());
        return deviceAdapter.characteristicsAreAvailable();
    }

    public boolean transmitMessage(SendMessage message) {
        logger.debug("{}: transmitMessage {}", deviceAdapter.getAddress(), message);
        return deviceAdapter.writeCharacteristic(message);
    }

    public void handleMessage(EncodedReceiveMessage message) {
        logger.debug("{}: handleMessage {}", deviceAdapter.getAddress(), message);
        message.decodeAndNotify(updateListener);
    }

    public synchronized void notifyCommandProcessingRequest() {
        logger.debug("{}: notifyCommandProcessingRequest", deviceAdapter.getAddress());
        context.getExecutorService().execute(() -> {
            currentState.notifyCommandProcessingRequest();
        });
    }

    @Override
    public synchronized void notifyReceivedSignalStrength(int rssi) {
        logger.debug("{}: RSSI {}", deviceAdapter.getAddress(), rssi);
        context.getExecutorService().execute(() -> {
            if (rssi >= context.getMinimalSignalStrengthForAcceptingCommunicationToDevice()) {
                currentState.indicateReceivedSignalStrength(rssi);
            } else {
                currentState.indicateSignalLoss();
            }
        });
    }

    @Override
    public synchronized void notifyServicesDiscovered() {
        logger.debug("{}: notifyServicesDiscovered", deviceAdapter.getAddress());
        context.getExecutorService().execute(() -> {
            currentState.notifyServicesDiscovered();
        });
    }

    @Override
    public synchronized void notifyConnectionEstablished() {
        logger.debug("{}: notifyConnectionEstablished", deviceAdapter.getAddress());
        context.getExecutorService().execute(() -> {
            currentState.notifyConnectionEstablished();
        });
    }

    @Override
    public synchronized void notifyConnectionClosed() {
        logger.debug("{}: notifyConnectionClosed", deviceAdapter.getAddress());
        context.getExecutorService().execute(() -> {
            currentState.notifyConnectionClosed();
        });
    }

    @Override
    public synchronized void notifyCharacteristicUpdate(EncodedReceiveMessage message) {
        logger.debug("{}: notifyCharacteristicUpdate {}", deviceAdapter.getAddress(), message);
        context.getExecutorService().execute(() -> {
            currentState.notifyCharacteristicUpdate(message);
        });
    }

    @Override
    public synchronized void notifyCharacteristicWritten() {
        logger.debug("{}: notifyCharacteristicWritten", deviceAdapter.getAddress());
        context.getExecutorService().execute(() -> {
            currentState.notifyMessageTransmitted();
        });
    }
}

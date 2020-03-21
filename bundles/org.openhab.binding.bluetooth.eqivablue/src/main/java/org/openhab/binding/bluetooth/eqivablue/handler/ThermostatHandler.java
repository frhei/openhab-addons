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
package org.openhab.binding.bluetooth.eqivablue.handler;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.BridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.bluetooth.BluetoothAdapter;
import org.openhab.binding.bluetooth.BluetoothAddress;
import org.openhab.binding.bluetooth.BluetoothBindingConstants;
import org.openhab.binding.bluetooth.BluetoothDevice;
import org.openhab.binding.bluetooth.eqivablue.EqivaBlueBindingConstants;
import org.openhab.binding.bluetooth.eqivablue.communication.CommandHandler;
import org.openhab.binding.bluetooth.eqivablue.communication.EqivablueDeviceAdapter;
import org.openhab.binding.bluetooth.eqivablue.communication.states.DeviceContext;
import org.openhab.binding.bluetooth.eqivablue.communication.states.DeviceHandler;
import org.openhab.binding.bluetooth.eqivablue.communication.states.Trace;
import org.openhab.binding.bluetooth.eqivablue.internal.OperatingMode;
import org.openhab.binding.bluetooth.eqivablue.internal.PresetTemperature;
import org.openhab.binding.bluetooth.eqivablue.internal.ThermostatUpdateListener;
import org.openhab.binding.bluetooth.eqivablue.internal.messages.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EqivaBlueHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Frank Heister - Initial contribution
 */
@NonNullByDefault
public class ThermostatHandler extends BaseThingHandler implements ThermostatUpdateListener {

    private final Logger logger = LoggerFactory.getLogger(ThermostatHandler.class);

    @NonNullByDefault({} /* non-null if initialized */)
    private BluetoothAdapter adapter;

    @NonNullByDefault({} /* non-null if initialized */)
    private BluetoothAddress address;

    @NonNullByDefault({} /* non-null if initialized */)
    private BluetoothDevice device;

    @NonNullByDefault({} /* non-null if initialized */)
    private EqivablueDeviceAdapter deviceAdapter;
    @NonNullByDefault({} /* non-null if initialized */)
    private DeviceContext deviceContext;
    @NonNullByDefault({} /* non-null if initialized */)
    private CommandHandler commandHandler;
    @NonNullByDefault({} /* non-null if initialized */)
    private DeviceHandler deviceHandler;
    @NonNullByDefault({} /* non-null if initialized */)

    private float ecoPresetTemperature;
    private float comfortPresetTemperature;

    public ThermostatHandler(Thing thing) {
        super(thing);
        ecoPresetTemperature = 17.0f;
        comfortPresetTemperature = 21.0f;
    }

    @Override
    @Trace
    public void initialize() {
        try {
            address = new BluetoothAddress(getConfig().get(BluetoothBindingConstants.CONFIGURATION_ADDRESS).toString());
        } catch (IllegalArgumentException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getLocalizedMessage());
            return;
        }

        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Not associated with any bridge");
            return;
        }

        BridgeHandler bridgeHandler = bridge.getHandler();
        if (!(bridgeHandler instanceof BluetoothAdapter)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Associated with an unsupported bridge");
            return;
        }

        adapter = (BluetoothAdapter) bridgeHandler;

        try {
            device = adapter.getDevice(address);
            deviceAdapter = new EqivablueDeviceAdapter(device);
            deviceContext = new DeviceContext();
            commandHandler = new CommandHandler();
            deviceHandler = new DeviceHandler(deviceAdapter, commandHandler, this, deviceContext);
        } finally {
        }

        updateStatus(ThingStatus.UNKNOWN);
    }

    @Override
    public void handleRemoval() {
        deviceHandler.dispose();
        deviceAdapter.dispose();
        super.handleRemoval();
    }

    @Override
    public void dispose() {
        super.dispose();
        handleRemoval();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("EqivaBlueHandler handling command {} on channel {}", command, channelUID);

        if (command == RefreshType.REFRESH) {
            getStatusFromRemoteDevice();
        } else {
            switch (channelUID.getId()) {
                case EqivaBlueBindingConstants.CHANNEL_TARGET_TEMPERATURE:
                    setTargetTemperature(command);
                    break;
                case EqivaBlueBindingConstants.CHANNEL_ECO_TEMPERATURE:
                    setEcoPresetTemperature(command);
                    break;
                case EqivaBlueBindingConstants.CHANNEL_COMFORT_TEMPERATURE:
                    setComfortPresetTemperature(command);
                    break;
                case EqivaBlueBindingConstants.CHANNEL_OPERATING_MODE:
                    setOperatingMode(command);
                    break;
                case EqivaBlueBindingConstants.CHANNEL_PRESET_TEMPERATURE:
                    selectPresetTemperature(command);
                    break;
                case EqivaBlueBindingConstants.CHANNEL_BOOST_MODE:
                    selectBoostMode(command);
                    break;
            }
        }
    }

    private void postCommandAndNotify(SendMessage theMessage) {
        commandHandler.add(theMessage);
        deviceHandler.notifyCommandProcessingRequest();
    }

    private void selectBoostMode(Command command) {
        boolean boostModeActivated = ((OnOffType) command) == OnOffType.ON;
        SendMessage messageToBeSent = SendMessage.setBoostMode(boostModeActivated);
        postCommandAndNotify(messageToBeSent);
    }

    private void selectPresetTemperature(Command command) {
        PresetTemperature presetTemperature = PresetTemperature.valueOf(((StringType) command).toString());
        if (presetTemperature != PresetTemperature.None) {
            SendMessage messageToBeSent = SendMessage.setPresetTemperature(presetTemperature);
            postCommandAndNotify(messageToBeSent);
        }
    }

    private void setOperatingMode(Command command) {
        OperatingMode operatingMode = OperatingMode.valueOf(((StringType) command).toString());
        SendMessage messageToBeSent = SendMessage.setOperatingModeMode(operatingMode);
        postCommandAndNotify(messageToBeSent);
    }

    private void setComfortPresetTemperature(Command command) {
        comfortPresetTemperature = ((DecimalType) command).floatValue();
        SendMessage messageToBeSent = SendMessage.setEcoAndComfortTemperature(comfortPresetTemperature,
                ecoPresetTemperature);
        postCommandAndNotify(messageToBeSent);
    }

    private void setEcoPresetTemperature(Command command) {
        ecoPresetTemperature = ((DecimalType) command).floatValue();
        SendMessage messageToBeSent = SendMessage.setEcoAndComfortTemperature(comfortPresetTemperature,
                ecoPresetTemperature);
        postCommandAndNotify(messageToBeSent);
    }

    private void getStatusFromRemoteDevice() {
        SendMessage messageToBeSent = SendMessage.queryStatus();
        postCommandAndNotify(messageToBeSent);
    }

    private void setTargetTemperature(Command command) {
        float targetTemperature = ((DecimalType) command).floatValue();
        SendMessage messageToBeSent = SendMessage.setTargetTemperature(targetTemperature);
        postCommandAndNotify(messageToBeSent);
    }

    @Override
    public void onTargetTemperatureUpdated(float temperature) {
        updateState(EqivaBlueBindingConstants.CHANNEL_TARGET_TEMPERATURE, new DecimalType(temperature));

        PresetTemperature temperaturePreset;
        if (Math.abs(temperature - EqivaBlueBindingConstants.ALWAYS_OFF_TEMPERATURE) < 0.1) {
            temperaturePreset = PresetTemperature.Off;
        } else if (Math.abs(temperature - EqivaBlueBindingConstants.ALWAYS_ON_TEMPERATURE) < 0.1) {
            temperaturePreset = PresetTemperature.On;
        } else if (Math.abs(temperature - ecoPresetTemperature) < 0.1) {
            temperaturePreset = PresetTemperature.Eco;
        } else if (Math.abs(temperature - comfortPresetTemperature) < 0.1) {
            temperaturePreset = PresetTemperature.Comfort;
        } else {
            temperaturePreset = PresetTemperature.None;
        }
        updateState(EqivaBlueBindingConstants.CHANNEL_PRESET_TEMPERATURE, new StringType(temperaturePreset.name()));
    }

    @Override
    public void onOperationModeUpdated(OperatingMode operationMode) {
        updateState(EqivaBlueBindingConstants.CHANNEL_OPERATING_MODE, new StringType(operationMode.name()));
    }

    @Override
    public void onVacationModeIsActive(boolean vacationModeIsActive) {
        updateState(EqivaBlueBindingConstants.CHANNEL_VACATION_MODE, OnOffType.from(vacationModeIsActive));
    }

    @Override
    public void onVacationModeEndDateTimeUpdate(LocalDateTime vacationEndDateTime) {
        updateState(EqivaBlueBindingConstants.CHANNEL_VACATION_MODE_DATE_TIME,
                new DateTimeType(vacationEndDateTime.atZone(ZoneId.systemDefault())));
    }

    @Override
    public void onWindowModeIsActive(boolean windowModeIsActive) {
        updateState(EqivaBlueBindingConstants.CHANNEL_WINDOW_MODE, OnOffType.from(windowModeIsActive));
    }

    @Override
    public void onBoostModeIsActive(boolean boostModeIsActive) {
        updateState(EqivaBlueBindingConstants.CHANNEL_BOOST_MODE, OnOffType.from(boostModeIsActive));
    }

    @Override
    public void onDaylightSavingTimeIsActive(boolean daylightSavingTimeIsActive) {
        updateState(EqivaBlueBindingConstants.CHANNEL_DAYLIGHT_SAVING_TIME, OnOffType.from(daylightSavingTimeIsActive));
    }

    @Override
    public void onUserLockIsActive(boolean userLockIsActive) {
        updateState(EqivaBlueBindingConstants.CHANNEL_DEVICE_LOCK, OnOffType.from(userLockIsActive));
    }

    @Override
    public void onBatteryLevelIsLow(boolean batteryIsLow) {
        updateState(EqivaBlueBindingConstants.CHANNEL_LOW_BATTERY, OnOffType.from(batteryIsLow));
    }

    @Override
    public void onValveStatusUpdated(int valveStatus) {
        updateState(EqivaBlueBindingConstants.CHANNEL_VALVE_STATUS, new DecimalType(valveStatus));
    }

    @Override
    public void updateThingStatus(ThingStatus status) {
        updateStatus(status);
    }

}

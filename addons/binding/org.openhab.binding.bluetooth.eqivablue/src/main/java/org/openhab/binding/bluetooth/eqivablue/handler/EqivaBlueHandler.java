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

import java.time.ZoneId;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.openhab.binding.bluetooth.BluetoothCompletionStatus;
import org.openhab.binding.bluetooth.BluetoothDevice.ConnectionState;
import org.openhab.binding.bluetooth.ConnectedBluetoothHandler;
import org.openhab.binding.bluetooth.eqivablue.EqivaBlueBindingConstants;
import org.openhab.binding.bluetooth.eqivablue.internal.EqivaBlueCommand;
import org.openhab.binding.bluetooth.eqivablue.internal.EqivaBlueStatus;
import org.openhab.binding.bluetooth.eqivablue.internal.OperatingMode;
import org.openhab.binding.bluetooth.eqivablue.internal.PresetTemperature;
import org.openhab.binding.bluetooth.notification.BluetoothConnectionStatusNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EqivaBlueHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Frank Heister - Initial contribution
 */
public class EqivaBlueHandler extends ConnectedBluetoothHandler {

    private final Logger logger = LoggerFactory.getLogger(EqivaBlueHandler.class);

    private final UUID UUID_EQIVA_BLUE_CONTROL_CHARACTERISTIC = UUID.fromString("3fa4585a-ce4a-3bad-db4b-b8df8179ea09");
    private final UUID UUID_EQIVA_BLUE_NOTIFICATION_CHARACTERISTIC = UUID
            .fromString("d0e8434d-cd29-0996-af41-6c90f4e0eb2a");

    // The characteristics we regularly use
    private BluetoothCharacteristic characteristicControl = null;
    private BluetoothCharacteristic characteristicNotification = null;
    private EqivaBlueStatus status;
    private boolean notificationsEnabled = false;
    private ScheduledFuture<?> queryStatusJob;
    private float currentComfortTemperature = 21.0f;
    private float currentEcoTemperature = 17.0f;
    private PresetTemperature currentPresetTemperature = PresetTemperature.None;

    public EqivaBlueHandler(Thing thing) {
        super(thing);
        logger.debug("EqivaBlueHandler created for {}", thing.getLabel());
    }

    @Override
    public void initialize() {
        super.initialize();
        logger.debug("EqivaBlueHandler initialized for {}", address);
        status = new EqivaBlueStatus();
    }

    @Override
    public void dispose() {
        cleanUp();
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("EqivaBlueHandler handling command {} on channel {}", command, channelUID);

        EqivaBlueCommand eqivaBlueCommand = null;
        if ((command == RefreshType.REFRESH)
                && ((channelUID.getId() == EqivaBlueBindingConstants.CHANNEL_TARGET_TEMPERATURE)
                        || (channelUID.getId() == EqivaBlueBindingConstants.CHANNEL_OPERATING_MODE)
                        || (channelUID.getId() == EqivaBlueBindingConstants.CHANNEL_VACATION_MODE)
                        || (channelUID.getId() == EqivaBlueBindingConstants.CHANNEL_BOOST_MODE)
                        || (channelUID.getId() == EqivaBlueBindingConstants.CHANNEL_DAYLIGHT_SAVING_TIME)
                        || (channelUID.getId() == EqivaBlueBindingConstants.CHANNEL_WINDOW_MODE)
                        || (channelUID.getId() == EqivaBlueBindingConstants.CHANNEL_DEVICE_LOCK)
                        || (channelUID.getId() == EqivaBlueBindingConstants.CHANNEL_LOW_BATTERY)
                        || (channelUID.getId() == EqivaBlueBindingConstants.CHANNEL_VALVE_STATUS))) {
            eqivaBlueCommand = EqivaBlueCommand.queryStatus();
        } else {
            switch (channelUID.getId()) {
                case EqivaBlueBindingConstants.CHANNEL_TARGET_TEMPERATURE:
                    if (command instanceof DecimalType) {
                        DecimalType decimalCommand = (DecimalType) command;
                        eqivaBlueCommand = EqivaBlueCommand.setTargetTemperature(decimalCommand.floatValue());
                    }
                    break;
                case EqivaBlueBindingConstants.CHANNEL_ECO_TEMPERATURE:
                    if (command instanceof DecimalType) {
                        DecimalType decimalCommand = (DecimalType) command;
                        currentEcoTemperature = decimalCommand.floatValue();
                    }
                    break;
                case EqivaBlueBindingConstants.CHANNEL_COMFORT_TEMPERATURE:
                    if (command instanceof DecimalType) {
                        DecimalType decimalCommand = (DecimalType) command;
                        currentComfortTemperature = decimalCommand.floatValue();
                    }
                    break;
                case EqivaBlueBindingConstants.CHANNEL_SET_ECO_AND_COMFORT_TEMPERATURE:
                    if (command instanceof DecimalType) {
                        eqivaBlueCommand = EqivaBlueCommand.setEcoAndComfortTemperature(currentComfortTemperature,
                                currentEcoTemperature);
                    }
                    break;
                case EqivaBlueBindingConstants.CHANNEL_OPERATING_MODE:
                    if (command instanceof StringType) {
                        try {
                            OperatingMode operatingMode = OperatingMode.valueOf(((StringType) command).toString());
                            eqivaBlueCommand = EqivaBlueCommand.setOperatingModeMode(operatingMode);
                        } catch (IllegalArgumentException e) {
                            logger.error("Illegal operation mode {}", ((StringType) command).toString());
                        }
                    }
                    break;
                case EqivaBlueBindingConstants.CHANNEL_PRESET_TEMPERATURE:
                    if (command instanceof StringType) {
                        try {
                            PresetTemperature presetTemperature = PresetTemperature
                                    .valueOf(((StringType) command).toString());
                            if (presetTemperature != PresetTemperature.None) {
                                eqivaBlueCommand = EqivaBlueCommand.setPresetTemperature(presetTemperature);
                            }
                        } catch (IllegalArgumentException e) {
                            logger.error("Illegal operation mode {}", ((StringType) command).toString());
                        }
                    }
                    break;
                case EqivaBlueBindingConstants.CHANNEL_BOOST_MODE:
                    if (command instanceof OnOffType) {
                        eqivaBlueCommand = EqivaBlueCommand.setBoostMode(((OnOffType) command) == OnOffType.ON);
                    }
                    break;
            }
        }
        if (eqivaBlueCommand != null) {
            final EqivaBlueCommand commandToBeExecuted = eqivaBlueCommand;
            scheduler.submit(() -> {
                executeCommand(commandToBeExecuted);
            });
        }
        super.handleCommand(channelUID, command);
    }

    // private void queryStatus() {
    // synchronized (characteristicControl) {
    // if (characteristicControl != null) {
    //
    // logger.debug("Querying status for {}", address);
    //
    // if (characteristicNotification != null) {
    // if (device.enableNotifications(characteristicNotification)) {
    // logger.debug("EqivaBlue notifications enabled");
    // } else {
    // logger.error("EqivaBlue notification NOT enabled");
    // }
    // }
    // EqivaBlueCommand.queryStatus().execute(device, characteristicControl);
    // } else {
    // logger.error("Eqiva Blue: Cannot query status. Unable to find control characteristic!");
    // }
    // }
    // }

    private void executeCommand(EqivaBlueCommand command) {
        synchronized (characteristicControl) {
            if (characteristicControl != null) {

                logger.debug("Sending command {}", thing.getLabel());

                if (characteristicNotification != null) {
                    if (device.enableNotifications(characteristicNotification)) {
                        logger.debug("EqivaBlue notifications enabled");
                    } else {
                        logger.error("EqivaBlue notification NOT enabled");
                    }
                }
                command.execute(device, characteristicControl);
            } else {
                logger.error("Eqiva Blue: Cannot execute command. Unable to find control characteristic!");
            }
        }
    }

    @Override
    public void onConnectionStateChange(BluetoothConnectionStatusNotification connectionNotification) {
        super.onConnectionStateChange(connectionNotification);
        logger.debug("Connection status of {} has changed to {}", address, connectionNotification.getConnectionState());
        switch (connectionNotification.getConnectionState()) {
            case CONNECTED:
                break;
            case DISCONNECTED:
                cleanUp();
                break;
            default:
                break;
        }
    }

    @Override
    public void onServicesDiscovered() {
        logger.debug("Services of {} discovered", address);
        super.onServicesDiscovered();

        // Everything is initialized now - get the characteristics we want to use
        if (characteristicControl == null) {
            characteristicControl = device.getCharacteristic(UUID_EQIVA_BLUE_CONTROL_CHARACTERISTIC);
            if (characteristicControl == null) {
                logger.error("EqivaBlue control characteristic not known after service discovery!");
                return;
            }
        }

        if (characteristicNotification == null) {
            characteristicNotification = device.getCharacteristic(UUID_EQIVA_BLUE_NOTIFICATION_CHARACTERISTIC);
            if (characteristicNotification == null) {
                logger.error("EqivaBlue notification characteristic not known after service discovery!");
                return;
            }
            notificationsEnabled = device.enableNotifications(characteristicNotification);
            if (notificationsEnabled) {
                logger.debug("EqivaBlue notifications enabled");
                queryStatusJob = scheduler.scheduleWithFixedDelay(() -> {
                    try {
                        if (device.getConnectionState() == ConnectionState.CONNECTED) {
                            executeCommand(EqivaBlueCommand.queryStatus());
                        }
                    } catch (Exception e) {
                        logger.debug("Exception occured in QueryStatusJob for EqivaBlue - Reason: {}", e.getMessage());
                    }
                }, 0, 60, TimeUnit.SECONDS);
            } else {
                logger.error("EqivaBlue notification NOT enabled");
            }

        }
    }

    @Override
    public void onCharacteristicWriteComplete(BluetoothCharacteristic characteristic,
            BluetoothCompletionStatus status) {
        logger.debug("Characteristics write for {} completed with status {}", address, status);
        // If this was a write to the control, then read back the state
        if (characteristic.getUuid().equals(UUID_EQIVA_BLUE_CONTROL_CHARACTERISTIC)) {
            if (status == BluetoothCompletionStatus.ERROR) {
                logger.debug("Wrote {} to characteristic {} of device {}: {}", characteristic.getByteValue(),
                        characteristic.getUuid(), address, status);
            }
        }
        super.onCharacteristicWriteComplete(characteristic, status);
    }

    @Override
    public void onCharacteristicUpdate(BluetoothCharacteristic characteristic) {
        logger.debug("Characteristics update received for {}", address);

        if (characteristic.getUuid().equals(UUID_EQIVA_BLUE_NOTIFICATION_CHARACTERISTIC)) {

            EqivaBlueStatus status = EqivaBlueStatus.createFrom(characteristic);
            switch (status.getStatusType()) {
                case General:
                    float targetTemperature = status.getTargetTemperature();
                    updateState(EqivaBlueBindingConstants.CHANNEL_TARGET_TEMPERATURE,
                            new DecimalType(targetTemperature));
                    updateState(EqivaBlueBindingConstants.CHANNEL_OPERATING_MODE,
                            new StringType(status.getOperatingMode().name()));
                    updateState(EqivaBlueBindingConstants.CHANNEL_VACATION_MODE,
                            OnOffType.from(status.isVacationMode()));
                    updateState(EqivaBlueBindingConstants.CHANNEL_VACATION_MODE_DATE_TIME,
                            new DateTimeType(status.getVacationDateTime().atZone(ZoneId.systemDefault())));
                    updateState(EqivaBlueBindingConstants.CHANNEL_BOOST_MODE, OnOffType.from(status.isBoostMode()));
                    updateState(EqivaBlueBindingConstants.CHANNEL_DAYLIGHT_SAVING_TIME,
                            OnOffType.from(status.isDstMode()));
                    updateState(EqivaBlueBindingConstants.CHANNEL_WINDOW_MODE, OnOffType.from(status.isWindowMode()));
                    updateState(EqivaBlueBindingConstants.CHANNEL_DEVICE_LOCK, OnOffType.from(status.isLockMode()));
                    updateState(EqivaBlueBindingConstants.CHANNEL_LOW_BATTERY, OnOffType.from(status.isLowBattery()));
                    updateState(EqivaBlueBindingConstants.CHANNEL_VALVE_STATUS,
                            new DecimalType(status.getValveState()));
                    if (Math.abs(targetTemperature - EqivaBlueBindingConstants.ALWAYS_OFF_TEMPERATURE) < 0.1) {
                        currentPresetTemperature = PresetTemperature.Off;
                    } else if (Math.abs(targetTemperature - EqivaBlueBindingConstants.ALWAYS_ON_TEMPERATURE) < 0.1) {
                        currentPresetTemperature = PresetTemperature.On;
                    } else if (Math.abs(targetTemperature - currentEcoTemperature) < 0.1) {
                        currentPresetTemperature = PresetTemperature.Eco;
                    } else if (Math.abs(targetTemperature - currentComfortTemperature) < 0.1) {
                        currentPresetTemperature = PresetTemperature.Comfort;
                    } else {
                        currentPresetTemperature = PresetTemperature.None;
                    }
                    updateState(EqivaBlueBindingConstants.CHANNEL_PRESET_TEMPERATURE,
                            new StringType(currentPresetTemperature.name()));
                    break;

                case TimeSchedule:
                    break;

                default:
                    logger.error("Invalid Status received");
                    break;
            }
        }
        super.onCharacteristicUpdate(characteristic);

    }

    private void cleanUp() {
        logger.debug("Cleaning up for {}", address);
        try {
            if (characteristicNotification != null) {
                device.disableNotifications(characteristicNotification);
            }
        } catch (IllegalStateException e) {
            logger.debug("Notification could not be disabled {}", e.getMessage());
        } finally {
            if (queryStatusJob != null) {
                queryStatusJob.cancel(true);
                queryStatusJob = null;
            }
            characteristicNotification = null;
            characteristicControl = null;
        }
    }
}

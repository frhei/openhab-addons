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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.openhab.binding.bluetooth.BluetoothDevice;
import org.openhab.binding.bluetooth.eqivablue.EqivaBlueBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Frank Heister - Initial contribution
 */
public class EncodedSendMessage {
    private final Logger logger = LoggerFactory.getLogger(EncodedSendMessage.class);
    private List<Integer> sequence = new ArrayList<Integer>();
    private final static int COMMAND_SET_DATETIME = 0x03;
    private final static int COMMAND_SET_ECO_AND_COMFORT_TEMPERATURE = 0x11;
    private final static int COMMAND_SET_OPERATING_MODE = 0x40;
    private final static int COMMAND_SET_TARGET_TEMPERATURE = 0x41;
    private final static int COMMAND_SWITCH_TO_COMFORT_TEMPERATURE = 0x43;
    private final static int COMMAND_SWITCH_TO_ECO_TEMPERATURE = 0x44;
    private final static int COMMAND_SET_BOOST_MODE = 0x45;

    private final static int PRIMITIVE_OPERATING_MODE_MANUAL = 0x40;
    private final static int PRIMITIVE_OPERATING_MODE_SCHEDULED = 0x00;
    private final static int PRIMITIVE_BOOST_MODE_ON = 0xFF;
    private final static int PRIMITIVE_BOOST_MODE_OFF = 0x00;

    // query status is done by updating the time on device
    public static EncodedSendMessage queryStatus() {
        return updateCurrentTime();
    }

    public static EncodedSendMessage updateCurrentTime() {
        LocalDateTime now = LocalDateTime.now();
        EncodedSendMessage command = new EncodedSendMessage();
        command.sequence.add(COMMAND_SET_DATETIME);
        command.sequence.add(now.getYear() % 100);
        command.sequence.add(now.getMonthValue());
        command.sequence.add(now.getDayOfMonth());
        command.sequence.add(now.getHour());
        command.sequence.add(now.getMinute());
        command.sequence.add(now.getSecond());
        return command;
    }

    public static EncodedSendMessage setTargetTemperature(float temperature) {
        EncodedSendMessage command = new EncodedSendMessage();
        command.sequence.add(COMMAND_SET_TARGET_TEMPERATURE);
        command.sequence.add(Math.round(temperature * 2));
        return command;
    }

    public static EncodedSendMessage setEcoAndComfortTemperature(float comfortTemperature, float ecoTemperature) {
        EncodedSendMessage command = new EncodedSendMessage();
        command.sequence.add(COMMAND_SET_ECO_AND_COMFORT_TEMPERATURE);
        command.sequence.add(Math.round(comfortTemperature * 2));
        command.sequence.add(Math.round(ecoTemperature * 2));
        return command;
    }

    public static EncodedSendMessage setBoostMode(boolean boostMode) {
        EncodedSendMessage command = new EncodedSendMessage();
        command.sequence.add(COMMAND_SET_BOOST_MODE);
        command.sequence.add(boostMode ? PRIMITIVE_BOOST_MODE_ON : PRIMITIVE_BOOST_MODE_OFF);
        return command;
    }

    public static EncodedSendMessage setOperatingModeMode(OperatingMode operatingMode) {
        EncodedSendMessage command = null;
        switch (operatingMode) {
            case Manual:
                command = new EncodedSendMessage();
                command.sequence.add(COMMAND_SET_OPERATING_MODE);
                command.sequence.add(PRIMITIVE_OPERATING_MODE_MANUAL);
                break;
            case Scheduled:
                command = new EncodedSendMessage();
                command.sequence.add(COMMAND_SET_OPERATING_MODE);
                command.sequence.add(PRIMITIVE_OPERATING_MODE_SCHEDULED);
                break;
            default:
                break;
        }
        return command;
    }

    public static EncodedSendMessage setPresetTemperature(PresetTemperature presetTemperature) {
        EncodedSendMessage command = null;
        switch (presetTemperature) {
            case On:
                command = EncodedSendMessage.setTargetTemperature(EqivaBlueBindingConstants.ALWAYS_ON_TEMPERATURE);
                break;
            case Off:
                command = EncodedSendMessage.setTargetTemperature(EqivaBlueBindingConstants.ALWAYS_OFF_TEMPERATURE);
                break;
            case Eco:
                command = new EncodedSendMessage();
                command.sequence.add(COMMAND_SWITCH_TO_ECO_TEMPERATURE);
                break;
            case Comfort:
                command = new EncodedSendMessage();
                command.sequence.add(COMMAND_SWITCH_TO_COMFORT_TEMPERATURE);
                break;
            default:
                break;
        }
        return command;
    }

    public void execute(BluetoothDevice device, BluetoothCharacteristic characteristic) {
        logger.debug("Eqiva Blue sent command {} to {}", sequence, device.getAddress());
        characteristic.setValue(sequence.stream().mapToInt(Integer::valueOf).toArray());
        device.writeCharacteristic(characteristic);
    }

}

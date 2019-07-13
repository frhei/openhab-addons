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
package org.openhab.binding.bluetooth.eqivablue.internal.messages;

import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.bluetooth.eqivablue.EqivaBlueBindingConstants;
import org.openhab.binding.bluetooth.eqivablue.internal.OperatingMode;
import org.openhab.binding.bluetooth.eqivablue.internal.PresetTemperature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Frank Heister - Initial contribution
 */
public class SendMessage {
    private final Logger logger = LoggerFactory.getLogger(SendMessage.class);
    protected List<Integer> sequence = new ArrayList<Integer>();
    protected final static int COMMAND_SET_DATETIME = 0x03;
    protected final static int COMMAND_SET_ECO_AND_COMFORT_TEMPERATURE = 0x11;
    protected final static int COMMAND_SET_OPERATING_MODE = 0x40;
    protected final static int COMMAND_SET_TARGET_TEMPERATURE = 0x41;
    protected final static int COMMAND_SWITCH_TO_COMFORT_TEMPERATURE = 0x43;
    protected final static int COMMAND_SWITCH_TO_ECO_TEMPERATURE = 0x44;
    protected final static int COMMAND_SET_BOOST_MODE = 0x45;

    protected final static int PRIMITIVE_OPERATING_MODE_MANUAL = 0x40;
    protected final static int PRIMITIVE_OPERATING_MODE_SCHEDULED = 0x00;
    protected final static int PRIMITIVE_BOOST_MODE_ON = 0xFF;
    protected final static int PRIMITIVE_BOOST_MODE_OFF = 0x00;

    // query status is done by updating the time on device
    public static SendMessage queryStatus() {
        return updateCurrentTime();
    }

    public static SendMessage updateCurrentTime() {
        return new UpdateCurrentTimeMessage();
    }

    public static SendMessage setTargetTemperature(float temperature) {
        return new SetTargetTemperatureMessage(temperature);
    }

    public static SendMessage setEcoAndComfortTemperature(float comfortTemperature, float ecoTemperature) {
        return new SetEcoAndComfortTemperatureMessage(comfortTemperature, ecoTemperature);
    }

    public static SendMessage setBoostMode(boolean boostMode) {
        return new SetBoostModeMessage(boostMode);
    }

    public static SendMessage setOperatingModeMode(OperatingMode operatingMode) {
        return new SetOperatingModeModeMessage(operatingMode);
    }

    public static SendMessage setPresetTemperature(PresetTemperature presetTemperature) {
        SendMessage command = null;
        switch (presetTemperature) {
            case On:
                command = new SetTargetTemperatureMessage(EqivaBlueBindingConstants.ALWAYS_ON_TEMPERATURE);
                break;
            case Off:
                command = new SetTargetTemperatureMessage(EqivaBlueBindingConstants.ALWAYS_OFF_TEMPERATURE);
                break;
            case Eco:
                command = new SwitchToEcoTemperature();
                break;
            case Comfort:
                command = new SwitchToComfortTemperature();
                break;
            default:
                command = new UpdateCurrentTimeMessage();
                break;
        }
        return command;
    }

    public int[] getEncodedContent() {
        return sequence.stream().mapToInt(Integer::valueOf).toArray();
    }

}

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
package org.openhab.binding.bluetooth.eqivablue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.bluetooth.BluetoothBindingConstants;

/**
 * The {@link EqivaBlueBindingConstants.EqivaBlueBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Frank Heister - Initial contribution
 */
@NonNullByDefault
public class EqivaBlueBindingConstants {

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_EQIVA_BLUE = new ThingTypeUID(BluetoothBindingConstants.BINDING_ID,
            "eqiva_blue");

    // List of all Channel ids
    public final static String CHANNEL_TARGET_TEMPERATURE = "targetTemperature";
    public final static String CHANNEL_ECO_TEMPERATURE = "ecoTemperature";
    public final static String CHANNEL_COMFORT_TEMPERATURE = "comfortTemperature";
    public final static String CHANNEL_SET_ECO_AND_COMFORT_TEMPERATURE = "setEcoAndcomfortTemperature";
    public final static String CHANNEL_OPERATING_MODE = "operatingMode";
    public final static String CHANNEL_PRESET_TEMPERATURE = "presetTemperature";
    public final static String CHANNEL_VACATION_MODE = "vacationMode";
    public final static String CHANNEL_VACATION_MODE_DATE_TIME = "vacationModeDateTime";
    public final static String CHANNEL_BOOST_MODE = "boostMode";
    public final static String CHANNEL_DAYLIGHT_SAVING_TIME = "daylightSavingTime";
    public final static String CHANNEL_WINDOW_MODE = "windowMode";
    public final static String CHANNEL_WINDOW_MODE_TEMPERATURE = "windowModeTemperature";
    public final static String CHANNEL_WINDOW_MODE_DURATION = "windowModeDuration";
    public final static String CHANNEL_DEVICE_LOCK = "deviceLock";
    public final static String CHANNEL_LOW_BATTERY = "lowBattery";
    public final static String CHANNEL_VALVE_STATUS = "valveStatus";
    public final static String CHANNEL_RSSI = "rssi";

    public final static String EQIVA_BLUE_NAME = "CC-RT-BLE";

    public final static float ALWAYS_OFF_TEMPERATURE = 4.5f;
    public final static float ALWAYS_ON_TEMPERATURE = 30.0f;

}

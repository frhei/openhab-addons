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

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Frank Heister - Initial contribution
 */
public class EncodedReceiveMessage {
    private final Logger logger = LoggerFactory.getLogger(EncodedReceiveMessage.class);

    private int[] encodedMessage = null;
    private ThermostatUpdateListener statusListener;
    private OperatingMode operatingMode;
    private boolean vacationModeIsActive;
    private boolean windowModeIsActive;
    private boolean boostModeIsActive;
    private boolean daylightSavingTimeIsActive;
    private boolean userLockIsActive;
    private boolean batteryLevelIsLow;
    private int valveState;
    private float targetTemperature;
    @SuppressWarnings("unused")
    private boolean unknownMode;

    private LocalDateTime vacationDateTime;
    private Map<DayOfWeek, List<ScheduleEntry>> timeSchedule = new HashMap<DayOfWeek, List<ScheduleEntry>>();

    public LocalDateTime getVacationDateTime() {
        return vacationDateTime;
    }

    private static final int MODE_MASK = 0x01;
    private static final int VACATION_MASK = 0x02;
    private static final int BOOST_MASK = 0x04;
    private static final int DST_MASK = 0x08;
    private static final int WINDOW_MASK = 0x10;
    private static final int LOCK_MASK = 0x20;
    private static final int UNKNOWN_MASK = 0x40;
    private static final int BATTERY_MASK = 0x80;

    public EncodedReceiveMessage(int[] encodedMessage, ThermostatUpdateListener aStatusListener) {
        this.encodedMessage = encodedMessage;
        this.statusListener = aStatusListener;
    }

    public void decodeAndNotify() {
        logger.debug("Decoding received message \"{}\"", encodedMessage);
        switch (encodedMessage[0]) {
            case 0x02:
                decodeGeneralStatus(Arrays.copyOfRange(encodedMessage, 1, encodedMessage.length));
                notifyGeneralStatus();
                logGeneralStatus();
                break;
            case 0x21:
                decodeTimeSchedule(Arrays.copyOfRange(encodedMessage, 1, encodedMessage.length));
                notfyTimeSchedule();
                logTimeSchedule();
                break;
            default:
                logger.error("unexpected first byte in received message! \"{} \"", encodedMessage[0]);
        }
    }

    private void logTimeSchedule() {
        // TODO Auto-generated method stub

    }

    private void notfyTimeSchedule() {
        // TODO Auto-generated method stub

    }

    private void logGeneralStatus() {
        logger.debug("EqivaBlue status update:");
        logger.debug("Temperature: {}", targetTemperature);
        logger.debug("Boost mode: {}", boostModeIsActive);
        logger.debug("Operating mode: {}", operatingMode);
        logger.debug("ValveState: {}", valveState);
        logger.debug("DST mode: {}", daylightSavingTimeIsActive);
        logger.debug("Window mode: {}", windowModeIsActive);
        logger.debug("Lock mode: {}", userLockIsActive);
        logger.debug("Low battery: {}", batteryLevelIsLow);
        logger.debug("Vacation mode: {}", vacationModeIsActive);
        logger.debug("Vacation DateTime: {}", vacationDateTime);
    }

    private void notifyGeneralStatus() {
        if (statusListener != null) {
            statusListener.onTargetTemperatureUpdated(targetTemperature);
            statusListener.onBoostModeIsActive(boostModeIsActive);
            statusListener.onOperationModeUpdated(operatingMode);
            statusListener.onValveStatusUpdated(valveState);
            statusListener.onDaylightSavingTimeIsActive(daylightSavingTimeIsActive);
            statusListener.onWindowModeIsActive(windowModeIsActive);
            statusListener.onUserLockIsActive(userLockIsActive);
            statusListener.onBatteryLevelIsLow(batteryLevelIsLow);
            statusListener.onVacationModeIsActive(vacationModeIsActive);
            statusListener.onVacationModeEndDateTimeUpdate(vacationDateTime);
        }
    }

    private void decodeTimeSchedule(int[] value) {
        DayOfWeek day = DayOfWeek.of(((value[0] + 5) % 7) + 1);
        List<ScheduleEntry> entryList = new ArrayList<ScheduleEntry>();
        timeSchedule.put(day, entryList);
        for (int i = 0; i < 7; i++) {
            int time = value[i * 2];
            int temperature = value[i * 2 + 1];
            if (temperature != 0) {
                entryList.add(ScheduleEntry.convertFromRaw(time, temperature));
            }
        }

    }

    private void decodeGeneralStatus(int[] value) {
        if (value[0] != 0x01) {
            logger.error("unexpected second byte in status result! \"{} \"", value[0]);
        }

        int statusByte = value[1];
        int valveByte = value[2];
        int temperatureByte = value[4];

        // temperatureByte == 0x09 => OFF
        // temperatureByte == 0x3c => ON

        if ((statusByte & MODE_MASK) == 0) {
            operatingMode = OperatingMode.Scheduled;
        } else {
            operatingMode = OperatingMode.Manual;
        }

        vacationModeIsActive = ((statusByte & VACATION_MASK) != 0);
        boostModeIsActive = ((statusByte & BOOST_MASK) != 0);
        daylightSavingTimeIsActive = ((statusByte & DST_MASK) != 0);
        windowModeIsActive = ((statusByte & WINDOW_MASK) != 0);
        userLockIsActive = ((statusByte & LOCK_MASK) != 0);
        unknownMode = ((statusByte & UNKNOWN_MASK) != 0);
        batteryLevelIsLow = ((statusByte & BATTERY_MASK) != 0);
        valveState = valveByte;
        targetTemperature = (float) temperatureByte / 2;

        if (vacationModeIsActive) {
            vacationDateTime = LocalDateTime.of(2000 + value[6], value[7], value[5], (value[8] / 2),
                    (value[8] % 2) * 30);
        } else {
            vacationDateTime = LocalDateTime.MAX;
        }
    }

}

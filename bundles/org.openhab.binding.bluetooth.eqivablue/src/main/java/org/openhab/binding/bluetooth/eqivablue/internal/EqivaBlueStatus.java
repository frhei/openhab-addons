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

import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Frank Heister - Initial contribution
 */
public class EqivaBlueStatus {
    private final Logger logger = LoggerFactory.getLogger(EqivaBlueStatus.class);

    public enum StatusType {
        General,
        TimeSchedule,
        Invalid
    }

    private int[] sequence = null;
    private StatusType statusType = StatusType.Invalid;
    private OperatingMode operatingMode;
    private boolean vacationMode;
    private boolean windowMode;
    private boolean boostMode;
    private boolean dstMode;
    private boolean lockMode;
    private boolean unknownMode;
    private boolean lowBattery;
    private int valveState;
    private float targetTemperature;

    public StatusType getStatusType() {
        return statusType;
    }

    public float getTargetTemperature() {
        return targetTemperature;
    }

    public OperatingMode getOperatingMode() {
        return operatingMode;
    }

    public boolean isVacationMode() {
        return vacationMode;
    }

    public boolean isWindowMode() {
        return windowMode;
    }

    public boolean isBoostMode() {
        return boostMode;
    }

    public boolean isDstMode() {
        return dstMode;
    }

    public boolean isLockMode() {
        return lockMode;
    }

    public boolean isUnknownMode() {
        return unknownMode;
    }

    public boolean isLowBattery() {
        return lowBattery;
    }

    public int getValveState() {
        return valveState;
    }

    public LocalDateTime getVacationDateTime() {
        return vacationDateTime;
    }

    private LocalDateTime vacationDateTime;
    private Map<DayOfWeek, List<ScheduleEntry>> timeSchedule = new HashMap<DayOfWeek, List<ScheduleEntry>>();

    private static final int MODE_MASK = 0x01;
    private static final int VACATION_MASK = 0x02;
    private static final int BOOST_MASK = 0x04;
    private static final int DST_MASK = 0x08;
    private static final int WINDOW_MASK = 0x10;
    private static final int LOCK_MASK = 0x20;
    private static final int UNKNOWN_MASK = 0x40;
    private static final int BATTERY_MASK = 0x80;

    public static EqivaBlueStatus createFrom(BluetoothCharacteristic characteristic) {
        EqivaBlueStatus status = new EqivaBlueStatus();
        status.sequence = characteristic.getValue();
        status.update();
        return status;
    }

    private void update() {
        logger.debug("EqivaBlue status update is \"{}\"", sequence);
        switch (sequence[0]) {
            case 0x02:
                statusType = StatusType.General;
                updateGeneralStatus(Arrays.copyOfRange(sequence, 1, sequence.length));
                break;

            case 0x21:
                statusType = StatusType.TimeSchedule;
                updateTimeSchedule(Arrays.copyOfRange(sequence, 1, sequence.length));
                break;

            default:
                logger.error("unexpected first byte in status result! \"{} \"", sequence[0]);

        }

    }

    private void updateTimeSchedule(int[] value) {
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

    private void updateGeneralStatus(int[] value) {
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

        vacationMode = ((statusByte & VACATION_MASK) != 0);
        boostMode = ((statusByte & BOOST_MASK) != 0);
        dstMode = ((statusByte & DST_MASK) != 0);
        windowMode = ((statusByte & WINDOW_MASK) != 0);
        lockMode = ((statusByte & LOCK_MASK) != 0);
        unknownMode = ((statusByte & UNKNOWN_MASK) != 0);
        lowBattery = ((statusByte & BATTERY_MASK) != 0);
        valveState = valveByte;
        targetTemperature = (float) temperatureByte / 2;

        if (vacationMode) {
            vacationDateTime = LocalDateTime.of(value[6], value[8], value[5], (value[7] / 2), (value[7] % 2) * 30);
        } else {
            vacationDateTime = LocalDateTime.MAX;
        }

        logger.debug("EqivaBlue status update:");
        logger.debug("Temperature: {}", targetTemperature);
        logger.debug("Boost mode: {}", boostMode);
        logger.debug("Operating mode: {}", operatingMode);
        logger.debug("ValveState: {}", valveState);
        logger.debug("DST mode: {}", dstMode);
        logger.debug("Window mode: {}", windowMode);
        logger.debug("Lock mode: {}", lockMode);
        logger.debug("Low battery: {}", lowBattery);
        logger.debug("Vacation mode: {}", vacationMode);
        logger.debug("Vacation DateTime: {}", vacationDateTime);
    }

}

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

import org.eclipse.jdt.annotation.NonNull;

/**
 * The {@link ThermostatUpdateListener} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Frank Heister - Initial contribution
 */
public interface ThermostatUpdateListener {
    void onTargetTemperatureUpdated(float temperature);

    void onOperationModeUpdated(@NonNull OperatingMode operationMode);

    void onVacationModeIsActive(boolean vacationModeIsActive);

    void onVacationModeEndDateTimeUpdate(@NonNull LocalDateTime vacationEndDateTime);

    void onWindowModeIsActive(boolean windowModeIsActive);

    void onBoostModeIsActive(boolean boostModeIsActive);

    void onDaylightSavingTimeIsActive(boolean daylightSavingTimeIsActive);

    void onUserLockIsActive(boolean userLockIsActive);

    void onBatteryLevelIsLow(boolean batteryIsLow);

    void onValveStatusUpdated(int valveStatus);
}

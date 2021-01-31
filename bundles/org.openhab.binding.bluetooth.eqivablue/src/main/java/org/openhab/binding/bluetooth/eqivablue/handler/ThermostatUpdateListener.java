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
package org.openhab.binding.bluetooth.eqivablue.handler;

import java.time.LocalDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingStatus;

/**
 * @author Frank Heister - Initial contribution
 */
@NonNullByDefault
public interface ThermostatUpdateListener {
    void onTargetTemperatureUpdated(float temperature);

    void onOperationModeUpdated(OperatingMode operationMode);

    void onVacationModeIsActive(boolean vacationModeIsActive);

    void onVacationModeEndDateTimeUpdate(LocalDateTime vacationEndDateTime);

    void onWindowModeIsActive(boolean windowModeIsActive);

    void onBoostModeIsActive(boolean boostModeIsActive);

    void onDaylightSavingTimeIsActive(boolean daylightSavingTimeIsActive);

    void onUserLockIsActive(boolean userLockIsActive);

    void onBatteryLevelIsLow(boolean batteryIsLow);

    void onValveStatusUpdated(int valveStatus);

    void updateThingStatus(ThingStatus status);

}

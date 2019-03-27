package org.openhab.binding.bluetooth.eqivablue.internal;

import java.time.LocalDateTime;

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
}

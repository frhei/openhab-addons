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

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;

/**
 * @author Frank Heister - Initial contribution
 */
@RunWith(Parameterized.class)
public class MessageDecodingTest {
    @Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {

                // pairwise testing of int[][3]
                // 0 0 0 0 0 0 0 0 => 0x00
                // 0 1 1 1 1 1 1 1 => 0x7f
                // 1 1 0 1 0 1 0 1 => 0xd5
                // 1 0 1 0 1 0 1 0 => 0xaa
                { new int[] { 0x02, 0x01, 0x00, 0x00, 0xff, 0x1e }, OperatingMode.Scheduled, false, false, false, false,
                        false, false, 0, 15.0f, LocalDateTime.MAX },
                { new int[] { 0x02, 0x01, 0x7f, 0x00, 0xff, 0x1e, 0x1e, 0x13, 0x03, 0x16 }, OperatingMode.Manual, true,
                        true, true, true, true, false, 0, 15.0f, LocalDateTime.of(2019, 03, 30, 11, 00) },
                { new int[] { 0x02, 0x01, 0xd5, 0x00, 0xff, 0x1e }, OperatingMode.Manual, false, true, false, true,
                        false, true, 0, 15.0f, LocalDateTime.MAX },
                { new int[] { 0x02, 0x01, 0xaa, 0x00, 0xff, 0x1e, 0x01, 0x13, 0x04, 0x2d }, OperatingMode.Scheduled,
                        true, false, true, false, true, true, 0, 15.0f, LocalDateTime.of(2019, 04, 01, 22, 30) },

                // testing extremes for temperature and valve values
                { new int[] { 0x02, 0x01, 0x00, 0x64, 0xff, 0x09 }, OperatingMode.Scheduled, false, false, false, false,
                        false, false, 100, 4.5f, LocalDateTime.MAX },
                { new int[] { 0x02, 0x01, 0x00, 0x01, 0xff, 0x3C }, OperatingMode.Scheduled, false, false, false, false,
                        false, false, 1, 30.0f, LocalDateTime.MAX } });
    };

    @Parameter(0)
    public int[] encodedMessage;
    @Parameter(1)
    public OperatingMode operatingMode;
    @Parameter(2)
    public boolean vacationModeIsActive;
    @Parameter(3)
    public boolean boostModeIsActive;
    @Parameter(4)
    public boolean daylightSavingTimeIsActive;
    @Parameter(5)
    public boolean windowModeIsActive;
    @Parameter(6)
    public boolean userLockIsActive;
    @Parameter(7)
    public boolean batteryLevelIsLow;
    @Parameter(8)
    public int valveState;
    @Parameter(9)
    public float targetTemperature;
    @Parameter(10)
    public LocalDateTime vacationEndDateTime;

    @Mock
    private ThermostatUpdateListener listener;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void checkSimpleUpdateMessage() {

        EncodedReceiveMessage message = new EncodedReceiveMessage(encodedMessage, listener);
        message.decodeAndNotify();

        verify(listener, times(1)).onOperationModeUpdated(operatingMode);
        verify(listener, times(1)).onVacationModeIsActive(vacationModeIsActive);
        verify(listener, times(1)).onVacationModeEndDateTimeUpdate(vacationEndDateTime);
        verify(listener, times(1)).onBoostModeIsActive(boostModeIsActive);
        verify(listener, times(1)).onDaylightSavingTimeIsActive(daylightSavingTimeIsActive);
        verify(listener, times(1)).onWindowModeIsActive(windowModeIsActive);
        verify(listener, times(1)).onUserLockIsActive(userLockIsActive);
        verify(listener, times(1)).onBatteryLevelIsLow(batteryLevelIsLow);
        verify(listener, times(1)).onValveStatusUpdated(valveState);
        verify(listener, times(1)).onTargetTemperatureUpdated(targetTemperature);
    }

}

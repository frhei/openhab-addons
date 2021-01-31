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
package org.openhab.binding.bluetooth.eqivablue.messages;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.Mock;
import org.openhab.binding.bluetooth.eqivablue.handler.OperatingMode;
import org.openhab.binding.bluetooth.eqivablue.handler.ThermostatUpdateListener;
import org.openhab.binding.bluetooth.eqivablue.internal.messages.EncodedReceiveMessage;

/**
 * @author Frank Heister - Initial contribution
 */
@DisplayName("MessageDecodingTests")
@NonNullByDefault
@SuppressWarnings("null")
class MessageDecodingTest {

    @Mock
    private @Nullable ThermostatUpdateListener updateListener;

    @BeforeEach
    public void setUp() {
        initMocks(this);
    }

    @DisplayName("Should decode message and notify the listener with correct update values")
    @ParameterizedTest(name = "{index} => encodedMessage={0} \r\n"
            + "\toperatingMode={1}, vacationModeIsActive={2}, boostModeIsActive={3}, \r\n"
            + "\tdaylightSavingTimeIsActive={4}, windowModeIsActive={5}, userLockIsActive={6}, \\r\\n"
            + "batteryLevelIsLow={7}, valveState={8}, targetTemperature={9}, vacationEndDateTimea={10}")
    @ArgumentsSource(EncodedMessageArgumentProvider.class)
    public void checkSimpleUpdateMessage(int[] encodedMessage, OperatingMode operatingMode,
            boolean vacationModeIsActive, boolean boostModeIsActive, boolean daylightSavingTimeIsActive,
            boolean windowModeIsActive, boolean userLockIsActive, boolean batteryLevelIsLow, int valveState,
            float targetTemperature, LocalDateTime vacationEndDateTime) {
        EncodedReceiveMessage message = new EncodedReceiveMessage(encodedMessage);

        var listener = updateListener;
        if (listener == null)
            throw new IllegalStateException();

        message.decodeAndNotify(listener);
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

    static class EncodedMessageArgumentProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(@Nullable ExtensionContext context) throws Exception {
            return Stream.of(
                    // pairwise testing of int[][3]
                    // 0 0 0 0 0 0 0 0 => 0x00
                    // 0 1 1 1 1 1 1 1 => 0x7f
                    // 1 1 0 1 0 1 0 1 => 0xd5
                    // 1 0 1 0 1 0 1 0 => 0xaa
                    Arguments.of(new int[] { 0x02, 0x01, 0x00, 0x00, 0xff, 0x1e }, OperatingMode.Scheduled, false,
                            false, false, false, false, false, 0, 15.0f, LocalDateTime.of(1970, 1, 1, 0, 0)),
                    Arguments.of(new int[] { 0x02, 0x01, 0x7f, 0x00, 0xff, 0x1e, 0x1e, 0x13, 0x03, 0x16 },
                            OperatingMode.Manual, true, true, true, true, true, false, 0, 15.0f,
                            LocalDateTime.of(2019, 03, 30, 11, 00)),
                    Arguments.of(new int[] { 0x02, 0x01, 0xd5, 0x00, 0xff, 0x1e }, OperatingMode.Manual, false, true,
                            false, true, false, true, 0, 15.0f, LocalDateTime.of(1970, 1, 1, 0, 0)),
                    Arguments.of(new int[] { 0x02, 0x01, 0xaa, 0x00, 0xff, 0x1e, 0x01, 0x13, 0x04, 0x2d },
                            OperatingMode.Scheduled, true, false, true, false, true, true, 0, 15.0f,
                            LocalDateTime.of(2019, 04, 01, 22, 30)),

                    // testing extremes for temperature and valve values
                    Arguments.of(new int[] { 0x02, 0x01, 0x00, 0x64, 0xff, 0x09 }, OperatingMode.Scheduled, false,
                            false, false, false, false, false, 100, 4.5f, LocalDateTime.of(1970, 1, 1, 0, 0)),
                    Arguments.of(new int[] { 0x02, 0x01, 0x00, 0x01, 0xff, 0x3C }, OperatingMode.Scheduled, false,
                            false, false, false, false, false, 1, 30.0f, LocalDateTime.of(1970, 1, 1, 0, 0)));

        }
    }

}

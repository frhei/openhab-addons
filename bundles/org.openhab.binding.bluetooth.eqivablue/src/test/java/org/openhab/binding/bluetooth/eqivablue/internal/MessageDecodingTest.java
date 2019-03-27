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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * Tests cases EncodedReceiveMessage
 *
 * @author Jan N. Klug - Initial contribution
 */
public class MessageDecodingTest {

    @Mock
    private ThermostatUpdateListener listener;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void checkSimpleUpdateMessage() {

        EncodedReceiveMessage message = new EncodedReceiveMessage(new int[] { 0x02, 0x01, 0x00, 0x00, 0xff, 0x1e },
                listener);
        message.decodeAndNotify();

        verify(listener, times(1)).onOperationModeUpdated(OperatingMode.Scheduled);
        verify(listener, times(1)).onTargetTemperatureUpdated(15.0f);
    }

}

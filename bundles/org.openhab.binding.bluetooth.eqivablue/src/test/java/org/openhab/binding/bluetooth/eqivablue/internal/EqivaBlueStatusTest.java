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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openhab.binding.bluetooth.BluetoothCharacteristic;

/**
 * Tests cases EqivaBlueStatus
 *
 * @author Jan N. Klug - Initial contribution
 */
public class EqivaBlueStatusTest {

    @Mock
    private BluetoothCharacteristic characteristic;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void checkWithFadingWithoutHold() {
        when(characteristic.getValue()).thenReturn(new int[] { 0x02, 0x01, 0x00, 0x00, 0xff, 0x1e });

        EqivaBlueStatus status = EqivaBlueStatus.createFrom(characteristic);

        assertThat(status.getOperatingMode(), is(OperatingMode.Scheduled));
        assertThat(status.getTargetTemperature(), is(15.0F));
    }

}

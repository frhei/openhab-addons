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
package org.openhab.binding.bluetooth.eqivablue.internal;

import java.time.LocalTime;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Frank Heister - Initial contribution
 */
@NonNullByDefault
public class ScheduleEntry {

    @SuppressWarnings("unused")
    private int temperature;
    LocalTime startTime = LocalTime.MIN;

    public static ScheduleEntry convertFromRaw(int time, int temperature) {
        ScheduleEntry entry = new ScheduleEntry();
        // temperature is given in 0.5°C steps
        entry.temperature = temperature / 2;
        // time is given in 10 minute steps == 600 seconds
        entry.startTime = LocalTime.ofSecondOfDay(time * 600);

        return entry;
    }
}

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
package org.openhab.binding.bluetooth.eqivablue.internal.messages;

import java.time.LocalDateTime;

/**
 * @author Frank Heister - Initial contribution
 */
public class UpdateCurrentTimeMessage extends SendMessage {

    public UpdateCurrentTimeMessage() {
        LocalDateTime now = LocalDateTime.now();
        sequence.add(COMMAND_SET_DATETIME);
        sequence.add(now.getYear() % 100);
        sequence.add(now.getMonthValue());
        sequence.add(now.getDayOfMonth());
        sequence.add(now.getHour());
        sequence.add(now.getMinute());
        sequence.add(now.getSecond());
    }

}

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
package org.openhab.binding.bluetooth.eqivablue.internal.messages;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bluetooth.eqivablue.internal.OperatingMode;

/**
 * @author Frank Heister - Initial contribution
 */
@NonNullByDefault
public class SetOperatingModeModeMessage extends SendMessage {
    public SetOperatingModeModeMessage(OperatingMode operatingMode) {
        switch (operatingMode) {
            case Manual:
                sequence.add(COMMAND_SET_OPERATING_MODE);
                sequence.add(PRIMITIVE_OPERATING_MODE_MANUAL);
                break;
            case Scheduled:
                sequence.add(COMMAND_SET_OPERATING_MODE);
                sequence.add(PRIMITIVE_OPERATING_MODE_SCHEDULED);
                break;
            default:
                break;
        }
    }
}

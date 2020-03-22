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

/**
 * @author Frank Heister - Initial contribution
 */
@NonNullByDefault
public class SetBoostModeMessage extends SendMessage {
    public SetBoostModeMessage(boolean boostMode) {
        sequence.add(COMMAND_SET_BOOST_MODE);
        sequence.add(boostMode ? PRIMITIVE_BOOST_MODE_ON : PRIMITIVE_BOOST_MODE_OFF);
    }
}

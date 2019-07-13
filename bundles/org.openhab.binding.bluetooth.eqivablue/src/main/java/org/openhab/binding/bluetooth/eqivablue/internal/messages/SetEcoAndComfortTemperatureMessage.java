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

/**
 * @author Frank Heister - Initial contribution
 */
public class SetEcoAndComfortTemperatureMessage extends SendMessage {
    public SetEcoAndComfortTemperatureMessage(float comfortTemperature, float ecoTemperature) {
        sequence.add(COMMAND_SET_ECO_AND_COMFORT_TEMPERATURE);
        sequence.add(Math.round(comfortTemperature * 2));
        sequence.add(Math.round(ecoTemperature * 2));
    }
}

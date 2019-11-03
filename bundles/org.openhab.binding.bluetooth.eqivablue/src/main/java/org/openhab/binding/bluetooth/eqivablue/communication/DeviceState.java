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
package org.openhab.binding.bluetooth.eqivablue.communication;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Frank Heister - Initial contribution
 */
@NonNullByDefault
abstract class DeviceState {

    protected DeviceHandler deviceHandler;

    DeviceState(DeviceHandler theHandler) {
        deviceHandler = theHandler;
    }

    protected DeviceStatus getStatus() {
        return DeviceStatus.UNDEFINED;
    }

    protected void indicateReceivedSignalStrength(int rssi) {

    }

    protected void indicateSignalLoss() {

    }

    protected void onEntry() {
    }

    protected void onExit() {
    }

    protected void notifyConnectionEstablished() {
    }

    protected void notifyConnectionClosed() {
    }

}

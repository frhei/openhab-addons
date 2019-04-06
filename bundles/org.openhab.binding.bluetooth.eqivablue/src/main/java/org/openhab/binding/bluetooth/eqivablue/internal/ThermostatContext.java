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

import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.bluetooth.BluetoothDevice;
import org.openhab.binding.bluetooth.eqivablue.handler.ThermostatHandler;

/**
 * @author Frank Heister - Initial contribution
 */
public class ThermostatContext {

    private static final String EQ3_THERMOSTAT_THREADPOOL_NAME = "eq3thermostat";
    private ScheduledExecutorService executorService;
    private String name;
    private BluetoothDevice bluetoothDevice;

    public String getName() {
        return name;
    }

    public ScheduledExecutorService getExecutorService() {
        return executorService;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public ThermostatContext(ThermostatHandler theThermostatHandler, BluetoothDevice theBluetoothDevice) {
        Thing thermostat = theThermostatHandler.getThing();
        name = thermostat.getLabel();
        executorService = ThreadPoolManager.getScheduledPool(EQ3_THERMOSTAT_THREADPOOL_NAME);
        bluetoothDevice = theBluetoothDevice;
    }

}

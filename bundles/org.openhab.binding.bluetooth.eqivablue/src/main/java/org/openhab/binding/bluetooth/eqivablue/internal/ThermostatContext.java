package org.openhab.binding.bluetooth.eqivablue.internal;

import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.bluetooth.BluetoothDevice;
import org.openhab.binding.bluetooth.eqivablue.handler.ThermostatHandler;

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

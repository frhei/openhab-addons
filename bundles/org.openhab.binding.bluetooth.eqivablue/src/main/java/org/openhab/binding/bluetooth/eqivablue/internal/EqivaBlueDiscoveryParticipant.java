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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.bluetooth.BluetoothBindingConstants;
import org.openhab.binding.bluetooth.discovery.BluetoothDiscoveryDevice;
import org.openhab.binding.bluetooth.discovery.BluetoothDiscoveryParticipant;
import org.openhab.binding.bluetooth.eqivablue.EqivaBlueBindingConstants;
import org.osgi.service.component.annotations.Component;

/**
 * @author Frank Heister - Initial contribution
 */
@NonNullByDefault
@Component(immediate = true)
public class EqivaBlueDiscoveryParticipant implements BluetoothDiscoveryParticipant {

    private static final UUID UUID_EQIVA_BLUE_SERVICE = UUID.fromString("3e135142-654f-9090-134a-a6ff5bb77046");

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(EqivaBlueBindingConstants.THING_TYPE_EQIVA_BLUE);
    }

    @Override
    public @Nullable ThingUID getThingUID(BluetoothDiscoveryDevice device) {
        if ((EqivaBlueBindingConstants.EQIVA_BLUE_NAME.equals(device.getName()))
                || (device.supportsService(UUID_EQIVA_BLUE_SERVICE))) {
            return new ThingUID(EqivaBlueBindingConstants.THING_TYPE_EQIVA_BLUE, device.getAdapter().getUID(),
                    device.getAddress().toString().toLowerCase().replace(":", ""));
        } else {
            return null;
        }
    }

    @Override
    public @Nullable DiscoveryResult createResult(BluetoothDiscoveryDevice device) {
        ThingUID thingUID = getThingUID(device);

        if (thingUID != null) {
            String label = device.getName();

            Map<String, Object> properties = new HashMap<>();
            properties.put(BluetoothBindingConstants.CONFIGURATION_ADDRESS, device.getAddress().toString());
            properties.put(Thing.PROPERTY_VENDOR, "Eqiva");
            Integer txPower = device.getTxPower();
            if (txPower != null) {
                properties.put(BluetoothBindingConstants.PROPERTY_TXPOWER, Integer.toString(txPower));
            }

            // Create the discovery result and add to the inbox
            return DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withRepresentationProperty(BluetoothBindingConstants.CONFIGURATION_ADDRESS)
                    .withBridge(device.getAdapter().getUID()).withLabel(label).build();
        } else {
            return null;
        }
    }

}

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
package org.openhab.binding.bluetooth.eqivablue.communication.states;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openhab.binding.bluetooth.eqivablue.communication.states.stages.GivenStage;
import org.openhab.binding.bluetooth.eqivablue.communication.states.stages.ThenStage;
import org.openhab.binding.bluetooth.eqivablue.communication.states.stages.WhenStage;

import com.tngtech.jgiven.annotation.As;
import com.tngtech.jgiven.junit5.JGivenExtension;
import com.tngtech.jgiven.junit5.ScenarioTest;

/**
 * @author Frank Heister - Initial contribution
 */
@NonNullByDefault
@ExtendWith(JGivenExtension.class)
public class SignalStrengthTests extends ScenarioTest<GivenStage, WhenStage, ThenStage> {

    @ParameterizedTest
    @CsvSource({ "-90, -91", "-90, -92", "-100, -101" })
    @As("Receiving signal strength: low signal strength has no effect")
    public void low_signal_strength_has_no_effect(int rssiThreshold, int receivedRssi) {
        // @formatter:off
        given().minimal_signal_strength_for_accepting_communication_is(rssiThreshold);
        when().adapter_received_signal_with_strength(receivedRssi);
        then().no_connection_request_is_issued();
        // @formatter:on
    }

    @ParameterizedTest
    @CsvSource({ "-90, -90", "-90, -89", "-100, -99" })
    @As("Receiving signal strength: connection request caused by sufficient signal strength")
    public void connection_request_caused_by_sufficient_signal_strength(int rssiThreshold, int receivedRssi) {
        // @formatter:off
        given().minimal_signal_strength_for_accepting_communication_is(rssiThreshold);
        when().adapter_received_signal_with_strength(receivedRssi);
        then().a_connection_request_is_issued();
        // @formatter:on
    }

    @Test
    @As("Receiving signal strength: connection request caused by sufficient signal strength")
    public void no_connection_request_caused_by_sufficient_signal_strength_when_services_and_characteristics_are_discovered() {
        // @formatter:off
        int arbitraryRssi = -45;
        given().minimal_signal_strength_for_accepting_communication_is(arbitraryRssi).
            and().characteristics_are_available();
        when().adapter_received_signal_with_strength(arbitraryRssi);
        then().no_connection_request_is_issued();
        // @formatter:on
    }

    @Test
    @As("Receiving signal strength: update device status caused by sufficient signal strength")
    public void update_device_status_caused_by_sufficient_signal_strength_when_services_and_characteristics_are_discovered() {
        // @formatter:off
        int arbitraryRssi = -45;
        given().minimal_signal_strength_for_accepting_communication_is(arbitraryRssi).
            and().characteristics_are_available();
        when().adapter_received_signal_with_strength(arbitraryRssi);
        then().the_thing_status_is_updated_to(ThingStatus.ONLINE);
        // @formatter:on
    }

}

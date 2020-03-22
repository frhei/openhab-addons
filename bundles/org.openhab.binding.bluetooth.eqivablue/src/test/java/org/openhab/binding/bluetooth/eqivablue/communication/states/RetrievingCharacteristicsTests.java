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
public class RetrievingCharacteristicsTests extends ScenarioTest<GivenStage, WhenStage, ThenStage> {

    @Test
    @As("Retrieving characteristics: failure caused by missing characteristics")
    public void failure_caused_by_missing_characteristics() {
        // @formatter:off
        given().the_adapter_will_accept_service_discovery_requests().
            and().the_communication_characteristics_will_not_be_detected();
        when().a_service_discovery_request_is_issued().
            and().services_are_discovered();
        then().device_status_is(ThingStatus.INITIALIZING);
        // @formatter:on
    }

    @Test
    @As("Retrieving characteristics: query of pending commands caused by successful characteristic retrieval")
    public void query_of_pending_commands_caused_by_successful_characteristic_retrieval() {
        // @formatter:off
        given().the_adapter_will_accept_service_discovery_requests().
            and().the_communication_characteristics_will_be_detected();
        when().a_service_discovery_request_is_issued().
            and().services_are_discovered();
        then().pending_commands_have_been_queried().
            and().device_status_is(ThingStatus.ONLINE);
        // @formatter:on
    }

    @Test
    @As("Retrieving characteristics: disconnect request caused by missing pending commands")
    public void disconnect_request_caused_by_missing_pending_commands() {
        // @formatter:off
        given().the_adapter_will_accept_service_discovery_requests().
            and().the_communication_characteristics_will_be_detected().
            and().no_commands_are_pending();
        when().a_service_discovery_request_is_issued().
            and().services_are_discovered();
        then().pending_commands_have_been_queried().
            and().$_disconnect_requests_are_sent_to_device(1);
        // @formatter:on
    }

    @Test
    @As("Retrieving characteristics: transmission request caused by pending commands")
    public void missing_disconnect_request_caused_by_pending_commands() {
        // @formatter:off
        given().the_adapter_will_accept_service_discovery_requests().
            and().the_communication_characteristics_will_be_detected().
            and().commands_are_pending();
        when().a_service_discovery_request_is_issued().
            and().services_are_discovered();
        then().a_command_is_read().
            and().a_transmission_is_requested();
        // @formatter:on
    }
}

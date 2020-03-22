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
public class WaitingForDisconnectTests extends ScenarioTest<GivenStage, WhenStage, ThenStage> {

    @Test
    @As("Waiting for diconnect: disconnect request caused by missing pending commands")
    public void disconnect_request_caused_by_missing_pending_commands() {
        // @formatter:off
        given().no_commands_are_pending();
        when().characteristics_are_retrieved();
        then().pending_commands_have_been_queried().
            and().$_disconnect_requests_are_sent_to_device(1).
            and().device_status_is(ThingStatus.ONLINE);
        // @formatter:on
    }

    @Test
    @As("Waiting for diconnect: going idle caused by missing pending commands after disconnect")
    public void going_idle_caused_by_missing_pending_commands_after_disconnect() {
        // @formatter:off
        given().no_commands_are_pending();
        when().characteristics_are_retrieved();
        given().no_commands_are_pending();
        when().the_device_indicates_a_disconnect();
        then().$_connection_requests_are_issued(1).
            and().device_status_is(ThingStatus.ONLINE);
        // @formatter:on
    }

    @Test
    @As("Waiting for diconnect: connection request caused by newly pending commands after disconnect")
    public void connection_request_caused_by_newly_pending_commands_after_disconnect() {
        // @formatter:off
        given().no_commands_are_pending();
        when().characteristics_are_retrieved();
        given().commands_are_pending();
        when().the_device_indicates_a_disconnect();
        then().$_connection_requests_are_issued(2).
            and().device_status_is(ThingStatus.ONLINE);
        // @formatter:on
    }
}

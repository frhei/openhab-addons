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
@SuppressWarnings("null")
@NonNullByDefault
@ExtendWith(JGivenExtension.class)
public class TransmittingMessageTests extends ScenarioTest<GivenStage, WhenStage, ThenStage> {

    @ParameterizedTest
    @CsvSource({ "5, 0, 1", "5, 1, 2", "5, 4, 5", "100, 99, 100" })
    @As("Transmitting message: retries caused by rejects")
    public void retries_caused_by_rejects(int maxRequests, int rejectedRequests, int actualRequests) {
        // @formatter:off
        given().maximum_number_of_rejected_transmission_requests_is(maxRequests).
            and().the_adapter_will_reject_$_consecutive_transmission_requests(rejectedRequests);
        when().a_transmission_request_is_issued();
        then().$_transmission_requests_are_issued(actualRequests).
            and().device_status_is(ThingStatus.ONLINE);
        // @formatter:on
    }

    @ParameterizedTest
    @CsvSource({ "5", "6", "10" })
    @As("Transmitting message: failure caused by too many rejects")
    public void failure_caused_by_too_many_rejects(int maxRequests) {
        // @formatter:off
        given().maximum_number_of_rejected_transmission_requests_is(maxRequests).
            and().the_adapter_will_reject_$_consecutive_transmission_requests(maxRequests);
        when().a_transmission_request_is_issued();
        then().$_transmission_requests_are_issued(maxRequests).
            and().device_status_is(ThingStatus.INITIALIZING);
        // @formatter:on
    }

    @Test
    @As("Transmitting message: no timeout handling prior to timeout")
    public void no_timeout_handling_prior_to_timeout() {
        // @formatter:off
        given().transmission_request_timeout_is(20000L).
            and().the_adapter_will_accept_transmission_requests();
        when().a_transmission_request_is_issued().
            and().time_elapses_by(19999L);
        then().$_disconnect_requests_are_sent_to_device(0).
            and().device_status_is(ThingStatus.ONLINE);
        // @formatter:on
    }

    @ParameterizedTest
    @CsvSource({ "1, 1, 2", "5, 0, 1", "5, 1, 2", "5, 5, 6", "100, 100, 101" })
    @As("Transmitting message: retries caused by timeouts")
    public void retries_caused_by_timeouts(int maxNumberOfTimeouts, int numberOfTimeouts, int actualRequests) {
        // @formatter:off
        given().transmission_request_timeout_is(30000L).
            and().maximum_number_of_timeouts_is(maxNumberOfTimeouts).
            and().the_adapter_will_accept_transmission_requests();
        when().a_transmission_request_is_issued().
            and().transmission_requests_time_out_$_consecutive_times(numberOfTimeouts);
        then().$_transmission_requests_are_issued(actualRequests).
            and().device_status_is(ThingStatus.ONLINE);
        // @formatter:on
    }

    @ParameterizedTest
    @CsvSource({ "5", "6", "10" })
    @As("Transmitting message: failure caused by too many timeouts")
    public void failure_caused_by_too_many_timeouts(int maxNumberOfTimeouts) {
        // @formatter:off
        given().transmission_request_timeout_is(30000L).
            and().maximum_number_of_timeouts_is(maxNumberOfTimeouts).
            and().the_adapter_will_accept_transmission_requests();
        when().a_transmission_request_is_issued().
            and().transmission_requests_time_out_$_consecutive_times(maxNumberOfTimeouts+1);
        then().$_transmission_requests_are_issued(maxNumberOfTimeouts+1).
            and().device_status_is(ThingStatus.INITIALIZING);
        // @formatter:on
    }

    @Test
    @As("Transmitting message: successful message transmission")
    public void successful_message_transmission() {
        // @formatter:off
        given().the_adapter_will_accept_transmission_requests();
        when().a_transmission_request_is_issued().
            and().the_transmission_request_is_acknowledged();
        then().waiting_for_response();
        // @formatter:on
    }

    @Test
    @As("Transmitting message: timeout handling deactivated when message transmitted")
    public void timeout_handling_deactivated_when_message_transmitted() {
        // @formatter:off
        given().the_adapter_will_accept_transmission_requests();
        when().a_transmission_request_is_issued().
            and().the_transmission_request_is_acknowledged();
        then().the_scheduled_timer_is_cancelled();
        // @formatter:on
    }

    @Test
    @As("Transmitting message: timeout handling deactivated when going into failure situation (too many rejects)")
    public void timeout_handling_deactivated_when_going_into_failure_situation_too_many_rejects() {
        // @formatter:off
        given().maximum_number_of_rejected_transmission_requests_is(1).
            and().the_adapter_will_reject_$_consecutive_transmission_requests(1);
        when().a_transmission_request_is_issued();
        then().the_scheduled_timer_is_cancelled();
        // @formatter:off
    }

    @Test
    @As("Transmitting message: timeout handling deactivated when disconnected")
    public void timeout_handling_deactivated_when_disconnected() {
        // @formatter:off
        given().the_adapter_will_accept_transmission_requests();
        when().a_transmission_request_is_issued().
            and().the_device_indicated_a_disconnect_$_consecutive_times(1);
        then().the_scheduled_timer_is_cancelled();
        // @formatter:off
    }
}

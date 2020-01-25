package org.openhab.binding.bluetooth.eqivablue.communication.states;

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

@ExtendWith(JGivenExtension.class)
public class WaitingForResponseTests extends ScenarioTest<GivenStage, WhenStage, ThenStage> {

    @Test
    @As("Waiting for repsonse: no timeout handling prior to timeout")
    public void no_timeout_handling_prior_to_timeout() {
        // @formatter:off
        given().response_timeout_is(20000L);
        when().a_transmission_request_is_sent_to_device().
            and().time_elapses_by(19999L);
        then().$_transmission_requests_are_issued(1).
            and().device_status_is(ThingStatus.ONLINE);
        // @formatter:on
    }

    @ParameterizedTest
    @CsvSource({ "1, 1, 2", "5, 0, 1", "5, 1, 2", "5, 5, 6", "100, 100, 101" })
    @As("Waiting for repsonse: retries caused by timeouts")
    public void retries_caused_by_timeouts(int maxNumberOfTimeouts, int numberOfTimeouts, int actualRequests) {
        // @formatter:off
        given().response_timeout_is(30000L).
            and().maximum_number_of_timeouts_is(maxNumberOfTimeouts);
        when().a_transmission_request_is_sent_to_device().
            and().response_times_out_$_consecutive_times(numberOfTimeouts);
        then().$_transmission_requests_are_issued(actualRequests).
            and().device_status_is(ThingStatus.ONLINE);
        // @formatter:on
    }

    @ParameterizedTest
    @CsvSource({ "5", "6", "10" })
    @As("Waiting for repsonse: failure caused by too many timeouts")
    public void failure_caused_by_too_many_timeouts(int maxNumberOfTimeouts) {
        // @formatter:off
        given().response_timeout_is(30000L).
            and().maximum_number_of_timeouts_is(maxNumberOfTimeouts);
        when().a_transmission_request_is_sent_to_device().
            and().response_times_out_$_consecutive_times(maxNumberOfTimeouts+1);
        then().$_transmission_requests_are_issued(maxNumberOfTimeouts+1).
            and().device_status_is(ThingStatus.INITIALIZING);
        // @formatter:on
    }

    @Test
    @As("Waiting for repsonse: successful response reception")
    public void successful_response_reception() {
        // @formatter:off
        given().nothing();
        when().a_transmission_request_is_sent_to_device().
            and().a_response_is_received();
        then().the_command_processing_is_finished().
            and().the_received_response_is_processed();
        // @formatter:on
    }

    @Test
    @As("Waiting for repsonse: timeout handling deactivated when message transmitted")
    public void timeout_handling_deactivated_when_message_transmitted() {
        // @formatter:off
        given().nothing();
        when().a_transmission_request_is_sent_to_device().
            and().a_response_is_received();
        then().the_scheduled_timer_is_cancelled();
        // @formatter:on
    }

    @Test
    @As("Waiting for repsonse: timeout handling deactivated when disconnected")
    public void timeout_handling_deactivated_when_disconnected() {
        // @formatter:off
        given().nothing();
        when().a_transmission_request_is_sent_to_device().
            and().the_device_indicated_a_disconnect_$_consecutive_times(1);
        then().the_scheduled_timer_is_cancelled();
        // @formatter:off
    }

    @Test
    @As("Waiting for repsonse: disconnect request after successful response reception caused by missing pending commands")
    public void disconnect_request_after_successful_response_reception_caused_by_missing_pending_commands() {
        // @formatter:off
        given().no_commands_are_pending();
        when().a_transmission_request_is_sent_to_device().
            and().a_response_is_received();
        then().$_disconnect_requests_are_sent_to_device(1).
            and().$_transmission_requests_are_issued(1);
        // @formatter:on
    }

    @Test
    @As("Waiting for repsonse: transmission request after successful response reception caused by pending commands")
    public void transmission_request_after_successful_response_reception_caused_by_pending_commands() {
        // @formatter:off
        given().commands_are_pending();
        when().a_transmission_request_is_sent_to_device().
            and().a_response_is_received();
        then().$_disconnect_requests_are_sent_to_device(0).
            and().$_transmission_requests_are_issued(2);
        // @formatter:on
    }
}

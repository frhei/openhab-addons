package org.openhab.binding.bluetooth.eqivablue.internal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openhab.binding.bluetooth.eqivablue.communication.DeviceStatus;

import com.tngtech.jgiven.junit5.JGivenExtension;
import com.tngtech.jgiven.junit5.ScenarioTest;

@ExtendWith(JGivenExtension.class)
public class DeviceHandlerConnectionRequestForServiceDiscoveryTests
        extends ScenarioTest<GivenStage, WhenStage, ThenStage> {

    @ParameterizedTest
    @CsvSource({ "5, 0, 1", "5, 1, 2", "5, 4, 5", "100, 99, 100" })
    public void repetitive_connection_requests_upon_connection_request_rejection(int maxRequests, int rejectedRequests,
            int actualRequests) {
        // @formatter:off
        given().maximum_number_of_rejected_connection_requests_is(maxRequests).
            and().the_adapter_will_reject_$_consecutive_connection_requests(rejectedRequests);
        when().a_connection_request_is_issued();
        then().$_connection_requests_are_issued(actualRequests).
            and().device_status_is(DeviceStatus.OFFLINE);
        // @formatter:on
    }

    @ParameterizedTest
    @CsvSource({ "5", "6", "10" })
    public void too_many_connection_request_rejections_result_in_failure(int maxRequests) {
        // @formatter:off
        given().maximum_number_of_rejected_connection_requests_is(maxRequests).
            and().the_adapter_will_reject_$_consecutive_connection_requests(maxRequests);
        when().a_connection_request_is_issued();
        then().$_connection_requests_are_issued(maxRequests).
            and().device_status_is(DeviceStatus.FAILURE);
        // @formatter:off
    }

    @Test
    public void when_connection_request_is_acknowledged_then_a_service_discovery_request_is_issued() {
        // @formatter:off
        given().the_adapter_will_accept_connection_requests();
        when().a_connection_request_is_issued().
            and().a_connection_is_established();
        then().a_service_discovery_request_is_issued();
        // @formatter:on
    }

    @Test
    public void connection_request_has_not_timed_out() {
        // @formatter:off
        given().connection_request_timeout_is(15000L).
            and().the_adapter_will_accept_connection_requests();
        when().a_connection_request_is_issued().
            and().time_elapses_by(14999L);
        then().$_connection_requests_are_issued(1).
            and().device_status_is(DeviceStatus.OFFLINE);
        // @formatter:on
    }

    @Test
    public void connection_request_has_timed_out_and_results_in_a_reconnect() {
        // @formatter:off
        given().connection_request_timeout_is(40000L).
            and().maximum_number_of_timeouts_is(1).
            and().the_adapter_will_accept_connection_requests();
        when().a_connection_request_is_issued().
            and().time_elapses_by(40001L);
        then().$_connection_requests_are_issued(2).
            and().device_status_is(DeviceStatus.OFFLINE);
        // @formatter:on
    }

    @ParameterizedTest
    @CsvSource({ "5, 0, 1", "5, 1, 2", "5, 5, 6", "100, 100, 101" })
    public void when_connection_requests_petetively_time_out_then_connection_requests_are_issued_and_finally_succeed(
            int maxNumberOfTimeouts, int numberOfTimeouts, int actualRequests) {
        // @formatter:off
        given().maximum_number_of_timeouts_is(maxNumberOfTimeouts).
            and().the_adapter_will_accept_connection_requests();
        when().a_connection_request_is_issued().
            and().connection_requests_time_out_$_consecutive_times(numberOfTimeouts);
        then().$_connection_requests_are_issued(actualRequests).
            and().device_status_is(DeviceStatus.OFFLINE);
        // @formatter:on
    }

    @ParameterizedTest
    @CsvSource({ "5", "6", "10" })
    public void when_too_many_connection_request_time_out_then_this_results_in_failure(int maxNumberOfTimeouts) {
        // @formatter:off
        given().the_adapter_will_accept_connection_requests().
            and().maximum_number_of_timeouts_is(maxNumberOfTimeouts);
        when().a_connection_request_is_issued().
            and().connection_requests_time_out_$_consecutive_times(maxNumberOfTimeouts+1);
        then().$_connection_requests_are_issued(maxNumberOfTimeouts+1).
            and().device_status_is(DeviceStatus.FAILURE);
        // @formatter:on
    }

    @Test
    public void when_connection_request_is_acknowledged_then_the_connection_request_timer_gets_deactivated() {
        // @formatter:off
        given().the_adapter_will_accept_connection_requests();
        when().a_connection_request_is_issued().
            and().a_connection_is_established();
        then().the_scheduled_timer_is_cancelled();
        // @formatter:on
    }

    @Test
    public void when_failing_due_to_exceeding_rejections_then_the_connection_request_timer_gets_deactivated() {
        // @formatter:off
        given().maximum_number_of_rejected_connection_requests_is(5).
            and().the_adapter_will_reject_$_consecutive_connection_requests(5);
        when().a_connection_request_is_issued();
        then().the_scheduled_timer_is_cancelled();
        // @formatter:off
    }
}

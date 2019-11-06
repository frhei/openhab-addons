package org.openhab.binding.bluetooth.eqivablue.internal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openhab.binding.bluetooth.eqivablue.communication.DeviceStatus;

import com.tngtech.jgiven.annotation.As;
import com.tngtech.jgiven.junit5.JGivenExtension;
import com.tngtech.jgiven.junit5.ScenarioTest;

@ExtendWith(JGivenExtension.class)
public class DeviceHandlerConnectionRequestForServiceDiscoveryTests
        extends ScenarioTest<GivenStage, WhenStage, ThenStage> {

    @ParameterizedTest
    @CsvSource({ "5, 0, 1", "5, 1, 2", "5, 4, 5", "100, 99, 100" })
    @As("Connecting for service discovery: retries caused by rejects")
    public void retries_caused_by_rejects(int maxRequests, int rejectedRequests, int actualRequests) {
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
    @As("Connecting for service discovery: failure caused by too many rejects")
    public void failure_caused_by_too_many_rejects(int maxRequests) {
        // @formatter:off
        given().maximum_number_of_rejected_connection_requests_is(maxRequests).
            and().the_adapter_will_reject_$_consecutive_connection_requests(maxRequests);
        when().a_connection_request_is_issued();
        then().$_connection_requests_are_issued(maxRequests).
            and().device_status_is(DeviceStatus.FAILURE);
        // @formatter:off
    }

    @Test
    @As("Connecting for service discovery: successful connection establishment")
    public void succesful_connection_establishment() {
        // @formatter:off
        given().the_adapter_will_accept_connection_requests();
        when().a_connection_request_is_issued().
            and().a_connection_is_established();
        then().a_service_discovery_request_is_issued();
        // @formatter:on
    }

    @Test
    @As("Connecting for service discovery: no timeout handling prior to timeout")
    public void no_timeout_handling_prior_to_timeout() {
        // @formatter:off
        given().connection_request_timeout_is(15000L).
            and().the_adapter_will_accept_connection_requests();
        when().a_connection_request_is_issued().
            and().time_elapses_by(14999L);
        then().$_connection_requests_are_issued(1).
            and().device_status_is(DeviceStatus.OFFLINE);
        // @formatter:on
    }

    @ParameterizedTest
    @CsvSource({ "1, 1, 2", "5, 0, 1", "5, 1, 2", "5, 5, 6", "100, 100, 101" })
    @As("Connecting for service discovery: retries caused by timeouts")
    public void retries_caused_by_timeouts(int maxNumberOfTimeouts, int numberOfTimeouts, int actualRequests) {
        // @formatter:off
        given().connection_request_timeout_is(40000L).
            and().maximum_number_of_timeouts_is(maxNumberOfTimeouts).
            and().the_adapter_will_accept_connection_requests();
        when().a_connection_request_is_issued().
            and().connection_requests_time_out_$_consecutive_times(numberOfTimeouts);
        then().$_connection_requests_are_issued(actualRequests).
            and().device_status_is(DeviceStatus.OFFLINE);
        // @formatter:on
    }

    @ParameterizedTest
    @CsvSource({ "5", "6", "10" })
    @As("Connecting for service discovery: failure caused by too many timeouts")
    public void failure_caused_by_too_many_timeouts(int maxNumberOfTimeouts) {
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
    @As("Connecting for service discovery: timeout handling deactivated when connection is established")
    public void when_connection_request_is_acknowledged_then_the_connection_request_timer_gets_deactivated() {
        // @formatter:off
        given().the_adapter_will_accept_connection_requests();
        when().a_connection_request_is_issued().
            and().a_connection_is_established();
        then().the_scheduled_timer_is_cancelled();
        // @formatter:on
    }

    @Test
    @As("Connecting for service discovery: timeout handling deactivated when going into failure situation (too many rejects)")
    public void timeout_handling_deactivated_when_going_into_failure_situation_too_many_rejects() {
        // @formatter:off
        given().maximum_number_of_rejected_connection_requests_is(5).
            and().the_adapter_will_reject_$_consecutive_connection_requests(5);
        when().a_connection_request_is_issued();
        then().the_scheduled_timer_is_cancelled();
        // @formatter:off
    }
}

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
public class DeviceHandlerServiceDiscoveryRequestTests extends ScenarioTest<GivenStage, WhenStage, ThenStage> {

    @ParameterizedTest
    @CsvSource({ "5, 0, 1", "5, 1, 2", "5, 4, 5", "100, 99, 100" })
    @As("Service Discovery: retries caused by rejects")
    public void retries_caused_by_rejects(int maxRequests, int rejectedRequests, int actualRequests) {
        // @formatter:off
        given().maximum_number_of_rejected_service_discovery_requests_is(maxRequests).
            and().the_adapter_will_reject_$_consecutive_service_discovery_requests(rejectedRequests);
        when().a_service_discovery_request_is_issued();
        then().$_service_discovery_requests_are_issued(actualRequests).
            and().device_status_is(DeviceStatus.OFFLINE);
        // @formatter:on
    }

    @ParameterizedTest
    @CsvSource({ "5", "6", "10" })
    @As("Service Discovery: failure caused by too many rejects")
    public void failure_caused_by_too_many_rejects(int maxRequests) {
        // @formatter:off
        given().maximum_number_of_rejected_service_discovery_requests_is(maxRequests).
            and().the_adapter_will_reject_$_consecutive_service_discovery_requests(maxRequests);
        when().a_service_discovery_request_is_issued();
        then().$_service_discovery_requests_are_issued(maxRequests).
            and().device_status_is(DeviceStatus.FAILURE);
        // @formatter:on
    }

    @ParameterizedTest
    @CsvSource({ "5, 0, 1", "5, 1, 2", "5, 5, 6", "100, 100, 101" })
    @As("Service Discovery: retries caused by remote disconnects")
    public void retries_caused_by_remote_disconnects(int maxNumberOfDisconnects, int numberOfDisconnects,
            int actualRequests) {
        // @formatter:off
        given().maximum_number_of_remote_disconnects_is(maxNumberOfDisconnects).
            and().the_adapter_will_accept_service_discovery_requests();
        when().a_service_discovery_request_is_issued().
            and().the_device_indicated_a_disconnect_$_consecutive_times(numberOfDisconnects);
        then().$_connection_requests_are_issued(actualRequests).
            and().$_service_discovery_requests_are_issued(actualRequests).
            and().device_status_is(DeviceStatus.OFFLINE);
        // @formatter:on
    }

    @ParameterizedTest
    @CsvSource({ "5", "6", "10" })
    @As("Service Discovery: failure caused by too many remote disconnects")
    public void failure_caused_by_too_many_remote_disconnects(int maxNumberOfDisconnects) {
        // @formatter:off
        given().maximum_number_of_remote_disconnects_is(maxNumberOfDisconnects).
            and().the_adapter_will_accept_service_discovery_requests();
        when().a_service_discovery_request_is_issued().
            and().the_device_indicated_a_disconnect_$_consecutive_times(maxNumberOfDisconnects+1);
        then().$_connection_requests_are_issued(maxNumberOfDisconnects+1).
            and().$_service_discovery_requests_are_issued(maxNumberOfDisconnects+1).
            and().device_status_is(DeviceStatus.FAILURE);
        // @formatter:on
    }

    @Test
    @As("Service Discovery: no timeout handling prior to timeout")
    public void no_timeout_handling_prior_to_timeout() {
        // @formatter:off
        given().service_discovery_timeout_is(20000L).
            and().the_adapter_will_accept_service_discovery_requests();
        when().a_service_discovery_request_is_issued().
            and().time_elapses_by(19999L);
        then().$_disconnect_requests_are_sent_to_device(0).
            and().device_status_is(DeviceStatus.OFFLINE);
        // @formatter:on
    }

    @ParameterizedTest
    @CsvSource({ "1, 1, 2", "5, 0, 1", "5, 1, 2", "5, 5, 6", "100, 100, 101" })
    @As("Service Discovery: retries caused by timeouts")
    public void retries_caused_by_timeouts(int maxNumberOfTimeouts, int numberOfTimeouts, int actualRequests) {
        // @formatter:off
        given().service_discovery_timeout_is(30000L).
            and().maximum_number_of_timeouts_is(maxNumberOfTimeouts).
            and().the_adapter_will_accept_service_discovery_requests();
        when().a_service_discovery_request_is_issued().
            and().service_discovery_requests_time_out_$_consecutive_times(numberOfTimeouts);
        then().$_disconnect_requests_are_sent_to_device(numberOfTimeouts).
            and().$_service_discovery_requests_are_issued(actualRequests).
            and().device_status_is(DeviceStatus.OFFLINE);
        // @formatter:on
    }

    @ParameterizedTest
    @CsvSource({ "5", "6", "10" })
    @As("Service Discovery: failure caused by too many timeouts")
    public void failure_caused_by_too_many_timeouts(int maxNumberOfTimeouts) {
        // @formatter:off
        given().service_discovery_timeout_is(30000L).
            and().maximum_number_of_timeouts_is(maxNumberOfTimeouts).
            and().the_adapter_will_accept_service_discovery_requests();
        when().a_service_discovery_request_is_issued().
            and().service_discovery_requests_time_out_$_consecutive_times(maxNumberOfTimeouts+1);
        then().$_disconnect_requests_are_sent_to_device(maxNumberOfTimeouts).
            and().$_service_discovery_requests_are_issued(maxNumberOfTimeouts+1).
            and().device_status_is(DeviceStatus.FAILURE);
        // @formatter:on
    }

    @Test
    @As("Service Discovery: successful service discovery")
    public void successful_service_discovery() {
        // @formatter:off
        given().the_adapter_will_accept_service_discovery_requests();
        when().a_service_discovery_request_is_issued().
            and().services_are_discovered();
        then().communication_characteristics_are_acquired();
        // @formatter:on
    }

    @Test
    @As("Service Discovery: timeout handling deactivated when services discovered")
    public void timeout_handling_deactivated_when_services_discovered() {
        // @formatter:off
        given().the_adapter_will_accept_service_discovery_requests();
        when().a_service_discovery_request_is_issued().
            and().services_are_discovered();
        then().the_scheduled_timer_is_cancelled();
        // @formatter:on
    }

    @Test
    @As("Service Discovery: timeout handling deactivated when going into failure situation (too many rejects)")
    public void timeout_handling_deactivated_when_going_into_failure_situation_too_many_rejects() {
        // @formatter:off
        given().maximum_number_of_rejected_service_discovery_requests_is(1).
            and().the_adapter_will_reject_$_consecutive_service_discovery_requests(1);
        when().a_service_discovery_request_is_issued();
        then().the_scheduled_timer_is_cancelled();
        // @formatter:off
    }

    @Test
    @As("Service Discovery: timeout handling deactivated when disconnected")
    public void timeout_handling_deactivated_when_disconnected() {
        // @formatter:off
        given().maximum_number_of_remote_disconnects_is(1).
            and().the_adapter_will_accept_service_discovery_requests();
        when().a_service_discovery_request_is_issued().
            and().the_device_indicated_a_disconnect_$_consecutive_times(1);
        then().the_scheduled_timer_is_cancelled();
        // @formatter:off
    }
}

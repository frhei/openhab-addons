package org.openhab.binding.bluetooth.eqivablue.internal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openhab.binding.bluetooth.eqivablue.communication.DeviceStatus;

import com.tngtech.jgiven.junit5.JGivenExtension;
import com.tngtech.jgiven.junit5.ScenarioTest;

@ExtendWith(JGivenExtension.class)
public class DeviceHandlerServiceDiscoveryRequestTests extends ScenarioTest<GivenStage, WhenStage, ThenStage> {

    @ParameterizedTest
    @CsvSource({ "5, 0, 1", "5, 1, 2", "5, 4, 5", "100, 99, 100" })
    public void repetitive_service_discovery_requests_upon_service_discovery_request_rejection(int maxRequests,
            int rejectedRequests, int actualRequests) {
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
    public void too_many_service_discovery_request_rejections_result_in_failure(int maxRequests) {
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
    public void repetitive_service_discovery_requests_upon_remote_disconnect(int maxNumberOfDisconnects,
            int numberOfDisconnects, int actualRequests) {
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
    public void too_many_remote_disconnects_during_service_discovery_result_in_failure(int maxNumberOfDisconnects) {
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
    public void service_discovery_has_not_timed_out() {
        // @formatter:off
        given().connection_request_timeout_is(100000L).
            and().service_discovery_timeout_is(20000L).
            and().the_adapter_will_accept_service_discovery_requests();
        when().a_service_discovery_request_is_issued().
            and().time_elapses_by(19999L);
        then().$_disconnect_requests_are_sent_to_device(0).
            and().device_status_is(DeviceStatus.OFFLINE);
        // @formatter:on
    }

    @Test
    public void service_discovery_has_timed_out_and_results_in_a_reconnect() {
        // @formatter:off
        given().connection_request_timeout_is(100000L).
            and().service_discovery_timeout_is(30000L).
            and().maximum_number_of_timeouts_is(1).
            and().the_adapter_will_accept_service_discovery_requests();
        when().a_service_discovery_request_is_issued().
            and().time_elapses_by(30001L);
        then().$_disconnect_requests_are_sent_to_device(1).
            and().$_connection_requests_are_issued(2).
            and().device_status_is(DeviceStatus.OFFLINE);
        // @formatter:on
    }

    @ParameterizedTest
    @CsvSource({ "5, 0, 1", "5, 1, 2", "5, 5, 6", "100, 100, 101" })
    public void repetitive_service_discovery_requests_upon_service_discovery_timeouts(int maxNumberOfTimeouts,
            int numberOfTimeouts, int actualRequests) {
        // @formatter:off
        given().connection_request_timeout_is(100000L).
            and().service_discovery_timeout_is(30000L).
            and().maximum_number_of_timeouts_is(maxNumberOfTimeouts).
            and().the_adapter_will_accept_service_discovery_requests();
        when().a_service_discovery_request_is_issued().
            and().service_discovery_requests_time_out_$_consecutive_t(numberOfTimeouts);
        then().$_disconnect_requests_are_sent_to_device(numberOfTimeouts).
            and().$_service_discovery_requests_are_issued(actualRequests).
            and().device_status_is(DeviceStatus.OFFLINE);
        // @formatter:on
    }

    @ParameterizedTest
    @CsvSource({ "5", "6", "10" })
    public void too_many_service_discovery_timeouts_result_in_failure(int maxNumberOfTimeouts) {
        // @formatter:off
        given().connection_request_timeout_is(100000L).
            and().service_discovery_timeout_is(30000L).
            and().maximum_number_of_timeouts_is(maxNumberOfTimeouts).
            and().the_adapter_will_accept_service_discovery_requests();
        when().a_service_discovery_request_is_issued().
            and().service_discovery_requests_time_out_$_consecutive_t(maxNumberOfTimeouts+1);
        then().$_disconnect_requests_are_sent_to_device(maxNumberOfTimeouts).
            and().$_service_discovery_requests_are_issued(maxNumberOfTimeouts+1).
            and().device_status_is(DeviceStatus.FAILURE);
        // @formatter:on
    }
}

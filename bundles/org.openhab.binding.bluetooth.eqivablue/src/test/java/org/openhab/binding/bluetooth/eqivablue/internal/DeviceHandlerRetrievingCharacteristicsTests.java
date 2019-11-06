package org.openhab.binding.bluetooth.eqivablue.internal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openhab.binding.bluetooth.eqivablue.communication.DeviceStatus;

import com.tngtech.jgiven.annotation.As;
import com.tngtech.jgiven.junit5.JGivenExtension;
import com.tngtech.jgiven.junit5.ScenarioTest;

@ExtendWith(JGivenExtension.class)
public class DeviceHandlerRetrievingCharacteristicsTests extends ScenarioTest<GivenStage, WhenStage, ThenStage> {

    @Test
    @As("Retrieving characteristics: failure caused by missing characteristics")
    public void failure_caused_by_missing_characteristics() {
        // @formatter:off
        given().the_adapter_will_accept_service_discovery_requests().
            and().the_communication_characteristics_will_not_be_detected();
        when().a_service_discovery_request_is_issued().
            and().services_are_discovered();
        then().device_status_is(DeviceStatus.FAILURE);
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
            and().$_disconnect_requests_are_sent_to_device(1).
            and().device_status_is(DeviceStatus.ONLINE);
        // @formatter:on
    }

    @Test
    @As("Retrieving characteristics: missing disconnect request caused by pending commands")
    public void missing_disconnect_request_caused_by_pending_commands() {
        // @formatter:off
        given().the_adapter_will_accept_service_discovery_requests().
            and().the_communication_characteristics_will_be_detected().
            and().commands_are_pending();
        when().a_service_discovery_request_is_issued().
            and().services_are_discovered();
        then().pending_commands_have_been_queried().
            and().$_disconnect_requests_are_sent_to_device(0).
            and().device_status_is(DeviceStatus.ONLINE);
        // @formatter:on
    }
}

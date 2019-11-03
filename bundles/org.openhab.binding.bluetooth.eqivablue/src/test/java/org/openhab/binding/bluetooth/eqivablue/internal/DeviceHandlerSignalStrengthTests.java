package org.openhab.binding.bluetooth.eqivablue.internal;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.tngtech.jgiven.junit5.JGivenExtension;
import com.tngtech.jgiven.junit5.ScenarioTest;

@ExtendWith(JGivenExtension.class)
public class DeviceHandlerSignalStrengthTests extends ScenarioTest<GivenStage, WhenStage, ThenStage> {

    @ParameterizedTest
    @CsvSource({ "-90, -91", "-90, -92", "-100, -101" })
    public void low_signal_strength_has_no_effect(int rssiThreshold, int receivedRssi) {
        given().minimal_signal_strength_for_accepting_communication_is(rssiThreshold);
        when().adapter_received_signal_with_strength(receivedRssi);
        then().no_connection_request_is_issued();
    }

    @ParameterizedTest
    @CsvSource({ "-90, -90", "-90, -89", "-100, -99" })
    public void signal_strength_sufficient_to_request_connection(int rssiThreshold, int receivedRssi) {
        given().minimal_signal_strength_for_accepting_communication_is(rssiThreshold);
        when().adapter_received_signal_with_strength(receivedRssi);
        then().a_connection_request_is_issued();
    }
}

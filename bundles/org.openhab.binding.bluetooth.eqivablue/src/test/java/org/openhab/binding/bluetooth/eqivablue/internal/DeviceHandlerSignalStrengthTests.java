package org.openhab.binding.bluetooth.eqivablue.internal;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.tngtech.jgiven.annotation.As;
import com.tngtech.jgiven.junit5.JGivenExtension;
import com.tngtech.jgiven.junit5.ScenarioTest;

@ExtendWith(JGivenExtension.class)
public class DeviceHandlerSignalStrengthTests extends ScenarioTest<GivenStage, WhenStage, ThenStage> {

    @ParameterizedTest
    @CsvSource({ "-90, -91", "-90, -92", "-100, -101" })
    @As("Receiving signal strength: low signal strength has no effect")
    public void low_signal_strength_has_no_effect(int rssiThreshold, int receivedRssi) {
        given().minimal_signal_strength_for_accepting_communication_is(rssiThreshold);
        when().adapter_received_signal_with_strength(receivedRssi);
        then().no_connection_request_is_issued();
    }

    @ParameterizedTest
    @CsvSource({ "-90, -90", "-90, -89", "-100, -99" })
    @As("Receiving signal strength: connection request caused by sufficient signal strength")
    public void connection_request_caused_by_sufficient_signal_strength(int rssiThreshold, int receivedRssi) {
        given().minimal_signal_strength_for_accepting_communication_is(rssiThreshold);
        when().adapter_received_signal_with_strength(receivedRssi);
        then().a_connection_request_is_issued();
    }
}

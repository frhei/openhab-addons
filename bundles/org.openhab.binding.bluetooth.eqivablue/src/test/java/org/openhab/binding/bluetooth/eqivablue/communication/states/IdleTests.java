package org.openhab.binding.bluetooth.eqivablue.communication.states;

import org.eclipse.smarthome.core.thing.ThingStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openhab.binding.bluetooth.eqivablue.communication.states.stages.GivenStage;
import org.openhab.binding.bluetooth.eqivablue.communication.states.stages.ThenStage;
import org.openhab.binding.bluetooth.eqivablue.communication.states.stages.WhenStage;

import com.tngtech.jgiven.annotation.As;
import com.tngtech.jgiven.junit5.JGivenExtension;
import com.tngtech.jgiven.junit5.ScenarioTest;

@ExtendWith(JGivenExtension.class)
public class IdleTests extends ScenarioTest<GivenStage, WhenStage, ThenStage> {

    @Test
    @As("Idle: device status is online when going idle")
    public void device_status_is_online_when_going_idle() {
        // @formatter:off
        given().nothing();
        when().going_idle();
        then().device_status_is(ThingStatus.ONLINE);
        // @formatter:on
    }

    @Test
    @As("Idle: connection request caused by command processing request")
    public void connection_request_caused_by_command_processing_request() {
        // @formatter:off
        given().nothing();
        when().going_idle().
            and().a_command_processing_is_requested();
        then().a_connection_request_is_issued();
        // @formatter:on
    }
}

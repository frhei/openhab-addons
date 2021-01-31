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
import org.openhab.core.thing.ThingStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

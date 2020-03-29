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
package org.openhab.binding.bluetooth.eqivablue.communication.states.stages;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.openhab.binding.bluetooth.eqivablue.handler.ThermostatUpdateListener;
import org.openhab.binding.bluetooth.eqivablue.internal.communication.CommandHandler;
import org.openhab.binding.bluetooth.eqivablue.internal.communication.EqivablueDeviceAdapter;
import org.openhab.binding.bluetooth.eqivablue.internal.communication.states.DeviceContext;
import org.openhab.binding.bluetooth.eqivablue.internal.communication.states.DeviceHandler;
import org.openhab.binding.bluetooth.eqivablue.internal.messages.EncodedReceiveMessage;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;

/**
 * @author Frank Heister - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
public class ThenStage extends Stage<ThenStage> {

    @ExpectedScenarioState
    @Mock
    private @Nullable EqivablueDeviceAdapter deviceAdapter;

    @ExpectedScenarioState
    @Mock
    private @Nullable DeviceContext context;

    @ExpectedScenarioState
    @Spy
    private @Nullable FakeScheduledExecutorService executorService;

    @ExpectedScenarioState
    private @Nullable TestContext testContext;

    @ExpectedScenarioState
    private @Nullable DeviceHandler deviceHandler;

    @ExpectedScenarioState
    @Mock
    private @Nullable CommandHandler commandHandler;

    @ExpectedScenarioState
    @Mock
    private @Nullable ThermostatUpdateListener thingListener;

    @ExpectedScenarioState
    @Mock
    private @Nullable EncodedReceiveMessage receivedMessage;

    public ThenStage no_connection_request_is_issued() {
        verify(deviceAdapter, never()).requestConnection();
        return this;
    }

    public ThenStage a_connection_request_is_issued() {
        verify(deviceAdapter, atLeastOnce()).requestConnection();
        return this;
    }

    public ThenStage $_connection_requests_are_issued(int actualAttemts) {
        verify(deviceAdapter, times(actualAttemts)).requestConnection();
        return this;
    }

    public ThenStage a_service_discovery_request_is_issued() {
        verify(deviceAdapter, atLeastOnce()).requestDiscoverServices();
        return this;
    }

    public ThenStage $_service_discovery_requests_are_issued(int actualAttemts) {
        verify(deviceAdapter, times(actualAttemts)).requestDiscoverServices();
        return this;
    }

    public ThenStage $_transmission_requests_are_issued(int actualAttemts) {
        verify(deviceAdapter, times(actualAttemts)).writeCharacteristic(any());
        return this;
    }

    public ThenStage $_disconnect_requests_are_sent_to_device(int numberOfDisconnectRequests) {
        verify(deviceAdapter, times(numberOfDisconnectRequests)).requestDisconnect();
        return this;
    }

    public ThenStage device_status_is(ThingStatus status) {
        assertThat(deviceHandler.getStatus(), is(status));
        return this;
    }

    public ThenStage the_scheduled_timer_is_cancelled() {
        assertThat(testContext.getLastScheduledFuture().isCancelled(), is(true));
        return this;
    }

    public ThenStage communication_characteristics_are_acquired() {
        verify(deviceAdapter, times(1)).getCharacteristics();
        return this;
    }

    public ThenStage pending_commands_have_been_queried() {
        verify(commandHandler, times(1)).areCommandsPending();
        return this;
    }

    public ThenStage a_transmission_is_requested() {
        verify(deviceAdapter, times(1)).writeCharacteristic(any());
        return this;
    }

    public ThenStage a_command_is_read() {
        verify(commandHandler, times(1)).peekCommand();
        return this;
    }

    public ThenStage waiting_for_response() {
        // to be defined
        return this;
    }

    public ThenStage the_command_processing_is_finished() {
        verify(commandHandler, times(1)).popCommand();
        return this;
    }

    public ThenStage the_received_response_is_processed() {
        verify(receivedMessage, times(1)).decodeAndNotify(any());
        return this;
    }

    public ThenStage the_thing_status_is_updated_to(ThingStatus status) {
        ArgumentCaptor<ThingStatus> captor = ArgumentCaptor.forClass(ThingStatus.class);
        verify(thingListener, atLeastOnce()).updateThingStatus(captor.capture());
        assertThat(captor.getValue(), is(status));
        return this;
    }

}

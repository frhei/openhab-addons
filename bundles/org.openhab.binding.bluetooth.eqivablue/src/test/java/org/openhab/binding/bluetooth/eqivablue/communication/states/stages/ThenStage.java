package org.openhab.binding.bluetooth.eqivablue.communication.states.stages;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.eclipse.smarthome.core.thing.ThingStatus;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.openhab.binding.bluetooth.eqivablue.communication.CommandHandler;
import org.openhab.binding.bluetooth.eqivablue.communication.EqivablueDeviceAdapter;
import org.openhab.binding.bluetooth.eqivablue.communication.states.DeviceContext;
import org.openhab.binding.bluetooth.eqivablue.communication.states.DeviceHandler;
import org.openhab.binding.bluetooth.eqivablue.internal.EncodedReceiveMessage;
import org.openhab.binding.bluetooth.eqivablue.internal.ThermostatUpdateListener;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;

public class ThenStage extends Stage<ThenStage> {

    @ExpectedScenarioState
    @Mock
    EqivablueDeviceAdapter deviceAdapter;

    @ExpectedScenarioState
    @Mock
    private DeviceContext context;

    @ExpectedScenarioState
    @Spy
    private FakeScheduledExecutorService executorService;

    @ExpectedScenarioState
    private TestContext testContext;

    @ExpectedScenarioState
    private DeviceHandler deviceHandler;

    @ExpectedScenarioState
    @Mock
    private CommandHandler commandHandler;

    @ExpectedScenarioState
    @Mock
    private ThermostatUpdateListener thingListener;

    @ExpectedScenarioState
    @Mock
    private EncodedReceiveMessage receivedMessage;

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

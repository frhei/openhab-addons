package org.openhab.binding.bluetooth.eqivablue.communication.states.stages;

import static org.mockito.MockitoAnnotations.initMocks;

import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.openhab.binding.bluetooth.eqivablue.communication.EqivablueDeviceAdapter;
import org.openhab.binding.bluetooth.eqivablue.communication.CommandHandler;
import org.openhab.binding.bluetooth.eqivablue.communication.states.DeviceContext;
import org.openhab.binding.bluetooth.eqivablue.communication.states.DeviceHandler;
import org.openhab.binding.bluetooth.eqivablue.internal.EncodedReceiveMessage;
import org.openhab.binding.bluetooth.eqivablue.internal.ThermostatUpdateListener;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.AfterScenario;
import com.tngtech.jgiven.annotation.BeforeScenario;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;

public class GivenStage extends Stage<GivenStage> {

    @ProvidedScenarioState
    @Mock
    EqivablueDeviceAdapter deviceAdapter;

    @ProvidedScenarioState
    @Mock
    private DeviceContext context;

    @ProvidedScenarioState
    @Spy
    private FakeScheduledExecutorService executorService;

    @ProvidedScenarioState
    @Spy
    private FakeClock clock;

    @ProvidedScenarioState
    private DeviceHandler deviceHandler;

    @ProvidedScenarioState
    private TestContext testContext = new TestContext();

    @ProvidedScenarioState
    @Mock
    private CommandHandler commandHandler;

    @ProvidedScenarioState
    @Mock
    private ThermostatUpdateListener thingListener;

    @ProvidedScenarioState
    @Mock
    private EncodedReceiveMessage receivedMessage;

    @BeforeScenario
    public void setUp() {
        long standardTimeoutinMilliSeconds = 12345L;
        initMocks(this);
        executorService.setClock(clock);
        Mockito.when(context.getExecutorService()).thenReturn(executorService);
        service_discovery_timeout_is(standardTimeoutinMilliSeconds);
        deviceHandler = new DeviceHandler(deviceAdapter, commandHandler, thingListener, context);
    }

    @AfterScenario
    public void tearDown() {
        deviceHandler = null;
    }

    public GivenStage minimal_signal_strength_for_accepting_communication_is(int rssi) {
        Mockito.when(context.getMinimalSignalStrengthForAcceptingCommunicationToDevice()).thenReturn(rssi);
        return this;
    }

    public GivenStage maximum_number_of_rejected_connection_requests_is(int maxRetries) {
        Mockito.when(context.getMaximalNumberOfRetries()).thenReturn(maxRetries);
        return this;
    }

    public GivenStage maximum_number_of_rejected_service_discovery_requests_is(int maxRetries) {
        Mockito.when(context.getMaximalNumberOfRetries()).thenReturn(maxRetries);
        return this;
    }

    public GivenStage maximum_number_of_rejected_transmission_requests_is(int maxRetries) {
        Mockito.when(context.getMaximalNumberOfRetries()).thenReturn(maxRetries);
        return this;
    }

    public GivenStage maximum_number_of_remote_disconnects_is(int maxAttempts) {
        Mockito.when(context.getMaximalNumberOfRetries()).thenReturn(maxAttempts);
        return this;
    }

    public GivenStage maximum_number_of_timeouts_is(int maxNumberOfTimeouts) {
        Mockito.when(context.getMaximalNumberOfRetries()).thenReturn(maxNumberOfTimeouts);
        return this;
    }

    public GivenStage the_adapter_will_accept_connection_requests() {
        Mockito.when(deviceAdapter.requestConnection()).thenReturn(true);
        return this;
    }

    public GivenStage the_adapter_will_accept_service_discovery_requests() {
        Mockito.when(deviceAdapter.requestDiscoverServices()).thenReturn(true);
        return this;
    }

    public GivenStage the_adapter_will_accept_transmission_requests() {
        Mockito.when(deviceAdapter.writeCharacteristic(ArgumentMatchers.any())).thenReturn(true);
        return this;
    }

    public GivenStage the_adapter_will_reject_$_consecutive_connection_requests(int rejectedAttempts) {
        Mockito.when(deviceAdapter.requestConnection()).thenAnswer(new RetryAnswer(rejectedAttempts));
        return this;
    }

    public GivenStage the_adapter_will_reject_$_consecutive_service_discovery_requests(int rejectedAttempts) {
        Mockito.when(deviceAdapter.requestDiscoverServices()).thenAnswer(new RetryAnswer(rejectedAttempts));
        return this;
    }

    public GivenStage the_adapter_will_reject_$_consecutive_transmission_requests(int rejectedAttempts) {
        Mockito.when(deviceAdapter.writeCharacteristic(ArgumentMatchers.any()))
                .thenAnswer(new RetryAnswer(rejectedAttempts));
        return this;
    }

    public GivenStage service_discovery_timeout_is(long timeout) {
        Mockito.when(context.getServiceDiscoveryTimeoutInMilliseconds()).thenReturn(timeout);
        return this;
    }

    public GivenStage connection_request_timeout_is(long timeout) {
        Mockito.when(context.getConnectionRequestTimeoutInMilliseconds()).thenReturn(timeout);
        return this;
    }

    public GivenStage transmission_request_timeout_is(long timeout) {
        Mockito.when(context.getTransmissionRequestTimeoutInMilliseconds()).thenReturn(timeout);
        return this;
    }

    public GivenStage response_timeout_is(long timeout) {
        Mockito.when(context.getResponseTimeoutInMilliseconds()).thenReturn(timeout);
        return this;
    }

    public GivenStage the_communication_characteristics_will_be_detected() {
        Mockito.when(deviceAdapter.getCharacteristics()).thenReturn(true);
        return this;
    }

    public GivenStage the_communication_characteristics_will_not_be_detected() {
        Mockito.when(deviceAdapter.getCharacteristics()).thenReturn(false);
        return this;
    }

    public GivenStage commands_are_pending() {
        Mockito.when(commandHandler.areCommandsPending()).thenReturn(true);
        return this;
    }

    public GivenStage no_commands_are_pending() {
        Mockito.when(commandHandler.areCommandsPending()).thenReturn(false);
        return this;
    }

    public GivenStage characteristics_are_available() {
        Mockito.when(deviceAdapter.characteristicsAreAvailable()).thenReturn(true);
        return this;
    }

    public GivenStage nothing() {
        return this;
    }
}

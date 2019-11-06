package org.openhab.binding.bluetooth.eqivablue.internal;

import java.time.Duration;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.openhab.binding.bluetooth.eqivablue.communication.BluetoothDeviceAdapter;
import org.openhab.binding.bluetooth.eqivablue.communication.DeviceContext;
import org.openhab.binding.bluetooth.eqivablue.communication.DeviceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.BeforeStage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;

public class WhenStage extends Stage<WhenStage> {

    private final Logger logger = LoggerFactory.getLogger(WhenStage.class);

    @ExpectedScenarioState
    @Mock
    BluetoothDeviceAdapter deviceAdapter;

    @ExpectedScenarioState
    @Mock
    private DeviceContext context;

    @ExpectedScenarioState
    @Spy
    private FakeScheduledExecutorService executorService;

    @ExpectedScenarioState
    @Spy
    private FakeClock clock;

    @ExpectedScenarioState
    private TestContext testContext;

    @ExpectedScenarioState
    private DeviceHandler deviceHandler;

    @BeforeStage
    public void beforeStage() {
        Mockito.clearInvocations(deviceAdapter);
    }

    public WhenStage adapter_received_signal_with_strength(int rssi) {
        deviceHandler.updateReceivedSignalStrength(rssi);
        return this;
    }

    public WhenStage a_connection_is_established() {
        deviceHandler.notifyConnectionEstablished();
        return this;
    }

    public WhenStage a_connection_request_is_issued() {
        int arbitraryRssi = -45;
        Mockito.when(context.getMinimalSignalStrengthForAcceptingCommunicationToDevice()).thenReturn(arbitraryRssi);
        executorService.addListener(testContext);
        deviceHandler.updateReceivedSignalStrength(arbitraryRssi);
        executorService.removeListener(testContext);
        return this;
    }

    public WhenStage a_service_discovery_request_is_issued() {
        int arbitraryRssi = -55;
        Mockito.when(context.getMinimalSignalStrengthForAcceptingCommunicationToDevice()).thenReturn(arbitraryRssi);
        Mockito.when(deviceAdapter.requestConnection()).thenReturn(true);
        deviceHandler.updateReceivedSignalStrength(arbitraryRssi);
        executorService.addListener(testContext);
        deviceHandler.notifyConnectionEstablished();
        executorService.removeListener(testContext);
        return this;
    }

    public WhenStage the_device_indicated_a_disconnect_$_consecutive_times(int maxNumberOfDisconnects) {
        int actualNumberOfDisconnects = 0;
        while (actualNumberOfDisconnects < maxNumberOfDisconnects) {
            deviceHandler.notifyConnectionClosed();
            deviceHandler.notifyConnectionEstablished();
            actualNumberOfDisconnects++;
        }
        return this;
    }

    public WhenStage service_discovery_requests_time_out_$_consecutive_times(int maxNumberOfTimeouts) {
        int actualNumberOfTimeouts = 0;
        while (actualNumberOfTimeouts < maxNumberOfTimeouts) {
            actualNumberOfTimeouts++;
            clock.elapse(Duration.ofMillis(context.getServiceDiscoveryTimeoutInMilliseconds()));
            executorService.run();
            deviceHandler.notifyConnectionEstablished();
        }
        return this;
    }

    public WhenStage connection_requests_time_out_$_consecutive_times(int maxNumberOfTimeouts) {
        int actualNumberOfTimeouts = 0;
        while (actualNumberOfTimeouts < maxNumberOfTimeouts) {
            actualNumberOfTimeouts++;
            clock.elapse(Duration.ofMillis(context.getConnectionRequestTimeoutInMilliseconds()));
            executorService.run();
        }
        return this;
    }

    public WhenStage time_elapses_by(long timeStep) {
        clock.elapse(Duration.ofMillis(timeStep));
        executorService.run();
        return this;
    }

    public WhenStage services_are_discovered() {
        deviceHandler.notifyServicesDiscovered();
        return this;
    }

}

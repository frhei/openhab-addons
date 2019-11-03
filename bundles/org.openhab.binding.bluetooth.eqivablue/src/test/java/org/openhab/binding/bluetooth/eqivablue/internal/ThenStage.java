package org.openhab.binding.bluetooth.eqivablue.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

import org.mockito.Mock;
import org.mockito.Spy;
import org.openhab.binding.bluetooth.eqivablue.communication.BluetoothDeviceAdapter;
import org.openhab.binding.bluetooth.eqivablue.communication.DeviceContext;
import org.openhab.binding.bluetooth.eqivablue.communication.DeviceHandler;
import org.openhab.binding.bluetooth.eqivablue.communication.DeviceStatus;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;

public class ThenStage extends Stage<ThenStage> {

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
    private TestContext testContext;

    @ExpectedScenarioState
    private DeviceHandler deviceHandler;

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

    public ThenStage $_disconnect_requests_are_sent_to_device(int numberOfDisconnectRequests) {
        verify(deviceAdapter, times(numberOfDisconnectRequests)).requestDisconnect();
        return this;
    }

    public ThenStage device_status_is(DeviceStatus status) {
        assertThat(deviceHandler.getStatus(), is(status));
        return this;
    }

    public ThenStage the_scheduled_timer_is_cancelled() {
        assertThat(testContext.getLastScheduledFuture().isCancelled(), is(true));
        return this;
    }
}

package org.openhab.binding.bluetooth.eqivablue.communication.states.stages;

import java.util.concurrent.ScheduledFuture;

import org.openhab.binding.bluetooth.eqivablue.communication.states.stages.FakeScheduledExecutorService.ScheduledFutureListener;

public class TestContext implements ScheduledFutureListener {
    private ScheduledFuture<?> lastScheduledFuture;

    public ScheduledFuture<?> getLastScheduledFuture() {
        return lastScheduledFuture;
    }

    public void setLastScheduledFuture(ScheduledFuture<?> lastScheduledFuture) {
        this.lastScheduledFuture = lastScheduledFuture;
    }

    @Override
    public void notifyScheduledCommand(ScheduledFuture<?> future) {
        setLastScheduledFuture(future);
    }
}

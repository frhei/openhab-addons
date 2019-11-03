package org.openhab.binding.bluetooth.eqivablue.internal;

import java.util.concurrent.ScheduledFuture;

public interface ScheduledFutureListener {
    public void notifyScheduledCommand(ScheduledFuture<?> future);
}

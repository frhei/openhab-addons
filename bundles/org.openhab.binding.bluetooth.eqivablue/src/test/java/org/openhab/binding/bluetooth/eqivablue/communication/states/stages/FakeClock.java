package org.openhab.binding.bluetooth.eqivablue.communication.states.stages;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

abstract class FakeClock extends Clock {
    private Instant now = Instant.ofEpochMilli(0);

    @Override
    public Instant instant() {
        return now;
    }

    void elapse(Duration duration) {
        now = now.plus(duration);
    }
}

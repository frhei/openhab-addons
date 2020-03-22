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

import java.util.concurrent.ScheduledFuture;

import org.openhab.binding.bluetooth.eqivablue.communication.states.stages.FakeScheduledExecutorService.ScheduledFutureListener;

/**
 * @author Frank Heister - Initial contribution
 */
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

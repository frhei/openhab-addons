/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.eqivablue.communication;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Frank Heister - Initial contribution
 */
@NonNullByDefault
public class ConnectingForServiceDiscoveryState extends OfflineState {

    private int numberOfSuccessiveTimeouts = 0;

    @Nullable
    private ScheduledFuture<?> timeoutHandler;

    ConnectingForServiceDiscoveryState(DeviceHandler theHandler) {
        super(theHandler);
    }

    @Override
    public void onEntry() {
        DeviceContext context = deviceHandler.getContext();

        long timeout = context.getConnectionRequestTimeoutInMilliseconds();
        timeoutHandler = context.getExecutorService().schedule(() -> connectionRequestTimedOut(), timeout,
                TimeUnit.MILLISECONDS);

        new RetryStrategy(deviceHandler::requestConnection, () -> {
            deviceHandler.setState(FailureState.class);
        }, context.getMaximalNumberOfRetries(), context).execute();
    }

    @Override
    public void onExit() {
        if (timeoutHandler != null) {
            timeoutHandler.cancel(true);
        }
    }

    private void connectionRequestTimedOut() {
        numberOfSuccessiveTimeouts++;
        if (numberOfSuccessiveTimeouts <= deviceHandler.getContext().getMaximalNumberOfRetries()) {
            deviceHandler.setState(ConnectingForServiceDiscoveryState.class);
        } else {
            numberOfSuccessiveTimeouts = 0;
            deviceHandler.setState(FailureState.class);
        }
    }

    @Override
    protected void notifyConnectionEstablished() {
        deviceHandler.setState(DiscoveringServicesState.class);
    }

}

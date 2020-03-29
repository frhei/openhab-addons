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
package org.openhab.binding.bluetooth.eqivablue.internal.communication.states;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Frank Heister - Initial contribution
 */
@NonNullByDefault
class ConnectingForCommandProcessingState extends OnlineState {

    private int numberOfSuccessiveTimeouts = 0;

    @Nullable
    private ScheduledFuture<?> timeoutHandler;

    ConnectingForCommandProcessingState(DeviceHandler theHandler) {
        super(theHandler);
    }

    @Override
    void onEntry() {
        DeviceContext context = deviceHandler.getContext();

        long timeout = context.getConnectionRequestTimeoutInMilliseconds();
        timeoutHandler = context.getExecutorService().schedule(() -> connectionRequestTimedOut(), timeout,
                TimeUnit.MILLISECONDS);

        new BooleanSupplierRetryStrategy(deviceHandler::requestConnection, () -> {
            deviceHandler.setState(FailureState.class);
        }, context.getMaximalNumberOfRetries(), context).execute();
    }

    @Override
    void onExit() {
        ScheduledFuture<?> localTimeoutHandler = timeoutHandler;
        if (localTimeoutHandler != null) {
            localTimeoutHandler.cancel(true);
        }
    }

    @Override
    void notifyConnectionEstablished() {
        deviceHandler.setState(TransmittingMessageState.class);
    }

    private void connectionRequestTimedOut() {
        numberOfSuccessiveTimeouts++;
        if (numberOfSuccessiveTimeouts <= deviceHandler.getContext().getMaximalNumberOfRetries()) {
            deviceHandler.setState(ConnectingForCommandProcessingState.class);
        } else {
            numberOfSuccessiveTimeouts = 0;
            deviceHandler.setState(FailureState.class);
        }
    }

}

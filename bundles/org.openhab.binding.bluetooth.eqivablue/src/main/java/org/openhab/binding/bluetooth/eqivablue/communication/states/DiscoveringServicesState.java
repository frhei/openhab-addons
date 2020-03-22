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
package org.openhab.binding.bluetooth.eqivablue.communication.states;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Frank Heister - Initial contribution
 */
@NonNullByDefault
class DiscoveringServicesState extends OfflineState {

    private int numberOfSuccessiveDisconnects = 0;
    private int numberOfSuccessiveTimeouts = 0;

    @Nullable
    private ScheduledFuture<?> timeoutHandler;

    DiscoveringServicesState(DeviceHandler theHandler) {
        super(theHandler);
    }

    @Override
    void onEntry() {
        DeviceContext context = deviceHandler.getContext();
        long timeout = context.getServiceDiscoveryTimeoutInMilliseconds();

        timeoutHandler = context.getExecutorService().schedule(() -> serviceDiscoveryTimedOut(), timeout,
                TimeUnit.MILLISECONDS);

        new BooleanSupplierRetryStrategy(deviceHandler::requestDiscoverServices, () -> {
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
    void notifyConnectionClosed() {
        numberOfSuccessiveDisconnects++;
        if (numberOfSuccessiveDisconnects <= deviceHandler.getContext().getMaximalNumberOfRetries()) {
            deviceHandler.setState(ConnectingForServiceDiscoveryState.class);
        } else {
            numberOfSuccessiveDisconnects = 0;
            deviceHandler.setState(FailureState.class);
        }
    }

    @Override
    void notifyServicesDiscovered() {
        deviceHandler.setState(RetrievingCharacteristicsState.class);
    }

    private void serviceDiscoveryTimedOut() {
        numberOfSuccessiveTimeouts++;
        if (numberOfSuccessiveTimeouts <= deviceHandler.getContext().getMaximalNumberOfRetries()) {
            deviceHandler.requestDisconnect();
            deviceHandler.setState(ConnectingForServiceDiscoveryState.class);
        } else {
            numberOfSuccessiveTimeouts = 0;
            deviceHandler.setState(FailureState.class);
        }
    }
}

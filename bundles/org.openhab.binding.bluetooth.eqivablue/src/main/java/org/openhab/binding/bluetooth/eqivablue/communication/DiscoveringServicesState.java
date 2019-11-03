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

import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Frank Heister - Initial contribution
 */
@NonNullByDefault
public class DiscoveringServicesState extends OfflineState {

    private int numberOfSuccessiveDisconnects = 0;
    private int numberOfSuccessiveTimeouts = 0;

    public DiscoveringServicesState(DeviceHandler theHandler) {
        super(theHandler);
    }

    @Override
    protected void onEntry() {
        DeviceContext context = deviceHandler.getContext();
        new RetryStrategy(deviceHandler::requestDiscoverServices, () -> {
            deviceHandler.setState(FailureState.class);
        }, context.getMaximalNumberOfRetries(), context).execute();

        long timeout = context.getServiceDiscoveryTimeoutInMilliseconds();
        context.getExecutorService().schedule(() -> serviceDiscoveryTimedOut(), timeout, TimeUnit.MILLISECONDS);
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

    @Override
    protected void notifyConnectionClosed() {
        numberOfSuccessiveDisconnects++;
        if (numberOfSuccessiveDisconnects <= deviceHandler.getContext().getMaximalNumberOfRetries()) {
            deviceHandler.setState(ConnectingForServiceDiscoveryState.class);
        } else {
            numberOfSuccessiveDisconnects = 0;
            deviceHandler.setState(FailureState.class);
        }
    }
}

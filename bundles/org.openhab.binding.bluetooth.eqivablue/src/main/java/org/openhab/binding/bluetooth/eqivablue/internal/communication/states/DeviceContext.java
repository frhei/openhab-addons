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

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.common.NamedThreadFactory;

/**
 * @author Frank Heister - Initial contribution
 */
@NonNullByDefault
public class DeviceContext {

    private static final long RETRY_TIMEOUT_IN_MILLISECONDS = 5000;
    private static final long SERVICE_DISCOVERY_TIMEOUT_IN_MILLISECONDS = 10000;
    private static final long CONNECTION_REQUEST_TIMEOUT_IN_MILLISECONDS = 10000;
    private static final long TRANSMISSION_REQUEST_TIMEOUT_IN_MILLISECONDS = 5000;
    private static final long RESPONSE_TIMEOUT_IN_MILLISECONDS = 10000;
    private static final int MINIMAL_SIGNAL_STRENGTH_FOR_ACCEPTING_COMMUNICATION_TO_DEVICE = -90;
    private static final int MAXIMAL_NUMBER_OF_RETRIES = 3;

    private ScheduledExecutorService executorService;

    public DeviceContext() {
        executorService = new ScheduledThreadPoolExecutor(5, new NamedThreadFactory("equivablue"));
    }

    public int getMinimalSignalStrengthForAcceptingCommunicationToDevice() {
        return MINIMAL_SIGNAL_STRENGTH_FOR_ACCEPTING_COMMUNICATION_TO_DEVICE;
    }

    public int getMaximalNumberOfRetries() {
        return MAXIMAL_NUMBER_OF_RETRIES;
    }

    public long getServiceDiscoveryTimeoutInMilliseconds() {
        return SERVICE_DISCOVERY_TIMEOUT_IN_MILLISECONDS;
    }

    public void waitForTimeout() {
        try {
            TimeUnit.MILLISECONDS.sleep(RETRY_TIMEOUT_IN_MILLISECONDS);
        } catch (InterruptedException e) {
        }
    }

    public ScheduledExecutorService getExecutorService() {
        return executorService;
    }

    public long getConnectionRequestTimeoutInMilliseconds() {
        return CONNECTION_REQUEST_TIMEOUT_IN_MILLISECONDS;
    }

    public long getTransmissionRequestTimeoutInMilliseconds() {
        return TRANSMISSION_REQUEST_TIMEOUT_IN_MILLISECONDS;
    }

    public long getResponseTimeoutInMilliseconds() {
        return RESPONSE_TIMEOUT_IN_MILLISECONDS;
    }

}

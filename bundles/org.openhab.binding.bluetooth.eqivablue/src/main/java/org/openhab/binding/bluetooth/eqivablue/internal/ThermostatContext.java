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
package org.openhab.binding.bluetooth.eqivablue.internal;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.bluetooth.eqivablue.handler.ThermostatHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Frank Heister - Initial contribution
 */
public class ThermostatContext {

    private static final String EQ3_THERMOSTAT_THREADPOOL_NAME = "eq3thermostat";
    private static final long MESSAGE_SENT_CONFIRMATION_TIMEOUT_IN_MILLISECONDS = 1000;
    private static final long MESSAGE_RESPONSE_RECEIVED_TIMEOUT_IN_MILLISECONDS = 1000;
    private final Logger logger = LoggerFactory.getLogger(ThermostatContext.class);
    private ScheduledExecutorService executorService;
    private String name;
    private Future<?> sendJob = null;

    public String getName() {
        return name;
    }

    public ThermostatContext(ThermostatHandler theThermostatHandler) {
        Thing thermostat = theThermostatHandler.getThing();
        name = thermostat.getLabel();
        executorService = ThreadPoolManager.getScheduledPool(EQ3_THERMOSTAT_THREADPOOL_NAME);
    }

    public void dispose() {
        cancelSendJob();
    }

    public void startSendJob(Runnable task) {
        sendJob = executorService.submit(task);
        logger.debug("SendJob started for {}", name);
    }

    public void cancelSendJob() {
        if ((sendJob != null) && (sendJob.isDone() == false)) {
            sendJob.cancel(true);
            sendJob = null;
            logger.debug("SendJob canceled for {}", name);
        }
    }

    public long getMessageSentConfirmationTimeoutInMilliseconds() {
        return MESSAGE_SENT_CONFIRMATION_TIMEOUT_IN_MILLISECONDS;
    }

    public long getMessageResponseReceivedTimeoutInMilliseconds() {
        return MESSAGE_RESPONSE_RECEIVED_TIMEOUT_IN_MILLISECONDS;
    }

}

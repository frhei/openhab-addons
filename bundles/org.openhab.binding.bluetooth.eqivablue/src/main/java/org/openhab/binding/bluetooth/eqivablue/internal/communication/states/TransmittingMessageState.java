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
import org.openhab.binding.bluetooth.eqivablue.internal.messages.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Frank Heister - Initial contribution
 */
@NonNullByDefault
class TransmittingMessageState extends ConnectedState {

    private final Logger logger = LoggerFactory.getLogger(TransmittingMessageState.class);

    private int numberOfSuccessiveTimeouts = 0;

    @Nullable
    private ScheduledFuture<?> timeoutHandler;

    TransmittingMessageState(DeviceHandler theHandler) {
        super(theHandler);
    }

    @Override
    void onEntry() {
        if (timeoutHandler == null) {
            DeviceContext context = deviceHandler.getContext();
            SendMessage message = deviceHandler.getCommandHandler().peekCommand();

            if (message != null) {
                long timeout = context.getTransmissionRequestTimeoutInMilliseconds();
                timeoutHandler = context.getExecutorService().schedule(() -> transmissionRequestTimedOut(), timeout,
                        TimeUnit.MILLISECONDS);

                new BooleanFunctionRetryStrategy<SendMessage>(deviceHandler::transmitMessage, () -> {
                    deviceHandler.setState(FailureState.class);
                }, context.getMaximalNumberOfRetries(), context).execute(message);
            } else {
                logger.error("Failed to retrieve a message from command handler in TransmittingMessageState!");
                deviceHandler.setState(FailureState.class);
            }
        }
    }

    @Override
    void onExit() {
        ScheduledFuture<?> localTimeoutHandler = timeoutHandler;
        if (localTimeoutHandler != null) {
            localTimeoutHandler.cancel(true);
        }
        timeoutHandler = null;
    }

    @Override
    void notifyMessageTransmitted() {
        deviceHandler.setState(WaitingForResponseState.class);
    }

    private void transmissionRequestTimedOut() {
        numberOfSuccessiveTimeouts++;
        if (numberOfSuccessiveTimeouts <= deviceHandler.getContext().getMaximalNumberOfRetries()) {
            deviceHandler.setState(TransmittingMessageState.class);
        } else {
            numberOfSuccessiveTimeouts = 0;
            deviceHandler.setState(FailureState.class);
        }
    }
}

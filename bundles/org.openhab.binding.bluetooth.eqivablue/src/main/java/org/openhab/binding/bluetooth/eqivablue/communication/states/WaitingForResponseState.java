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
import org.openhab.binding.bluetooth.eqivablue.communication.CommandHandler;
import org.openhab.binding.bluetooth.eqivablue.internal.EncodedReceiveMessage;

/**
 * @author Frank Heister - Initial contribution
 */
@NonNullByDefault
class WaitingForResponseState extends ConnectedState {

    private int numberOfSuccessiveTimeouts = 0;

    @Nullable
    private ScheduledFuture<?> timeoutHandler;

    WaitingForResponseState(DeviceHandler theHandler) {
        super(theHandler);
    }

    @Override
    void onEntry() {
        DeviceContext context = deviceHandler.getContext();

        long timeout = context.getResponseTimeoutInMilliseconds();
        timeoutHandler = context.getExecutorService().schedule(() -> responseTimedOut(), timeout,
                TimeUnit.MILLISECONDS);

    }

    @Override
    void onExit() {
        ScheduledFuture<?> localTimeoutHandler = timeoutHandler;
        if (localTimeoutHandler != null) {
            localTimeoutHandler.cancel(true);
        }
    }

    @Override
    void notifyCharacteristicUpdate(EncodedReceiveMessage message) {
        CommandHandler commandHandler = deviceHandler.getCommandHandler();
        commandHandler.popCommand();
        deviceHandler.handleMessage(message);
        if (commandHandler.areCommandsPending()) {
            deviceHandler.setState(TransmittingMessageState.class);
        } else {
            deviceHandler.setState(WaitingForDisconnectState.class);
        }
    }

    private void responseTimedOut() {
        numberOfSuccessiveTimeouts++;
        if (numberOfSuccessiveTimeouts <= deviceHandler.getContext().getMaximalNumberOfRetries()) {
            deviceHandler.setState(TransmittingMessageState.class);
        } else {
            numberOfSuccessiveTimeouts = 0;
            deviceHandler.setState(FailureState.class);
        }
    }
}

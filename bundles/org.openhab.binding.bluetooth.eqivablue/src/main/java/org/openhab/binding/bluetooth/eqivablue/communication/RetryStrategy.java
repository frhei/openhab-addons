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

import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Frank Heister - Initial contribution
 */
@NonNullByDefault
public class RetryStrategy {
    enum RequestResult {
        SUCCESSFUL,
        RETRY,
        FAILURE
    }

    Supplier<Boolean> functionToBeExecuted;
    Runnable executedInCaseOfFailure;
    int maximumNumberOfRetries;
    int numberOfRetries = 0;
    DeviceContext context;

    public RetryStrategy(Supplier<Boolean> theFunctionToBeExecuted, Runnable theFailureFunction,
            int theMaximumNumberOfRetries, DeviceContext theContext) {
        functionToBeExecuted = theFunctionToBeExecuted;
        executedInCaseOfFailure = theFailureFunction;
        maximumNumberOfRetries = theMaximumNumberOfRetries;
        context = theContext;
    }

    public void execute() {
        RequestResult connectionRequestResult;

        do {
            connectionRequestResult = tryExecute();
        } while (connectionRequestResult == RequestResult.RETRY);

        switch (connectionRequestResult) {
            case FAILURE:
                executedInCaseOfFailure.run();
                break;
            default:
                break;
        }
    }

    public RequestResult tryExecute() {
        boolean trialSuccessful = functionToBeExecuted.get();
        if (trialSuccessful == true) {
            numberOfRetries = 0;
            return RequestResult.SUCCESSFUL;
        } else {
            numberOfRetries++;
            if (numberOfRetries < maximumNumberOfRetries) {
                context.waitForTimeout();
                return RequestResult.RETRY;
            } else {
                return RequestResult.FAILURE;
            }
        }
    }
}

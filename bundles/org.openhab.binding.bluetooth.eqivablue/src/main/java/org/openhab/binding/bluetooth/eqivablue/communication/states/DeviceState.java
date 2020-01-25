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
package org.openhab.binding.bluetooth.eqivablue.communication.states;

import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.openhab.binding.bluetooth.eqivablue.internal.EncodedReceiveMessage;

/**
 * @author Frank Heister - Initial contribution
 */
@NonNullByDefault
abstract class DeviceState {

    DeviceHandler deviceHandler;

    DeviceState(DeviceHandler theHandler) {
        deviceHandler = theHandler;
    }

    ThingStatus getStatus() {
        return ThingStatus.UNKNOWN;
    }

    void indicateReceivedSignalStrength(int rssi) {
    }

    void indicateSignalLoss() {
    }

    void onEntry() {
    }

    void onExit() {
    }

    void notifyConnectionEstablished() {
    }

    void notifyConnectionClosed() {
    }

    void notifyServicesDiscovered() {
    }

    void notifyCommandProcessingRequest() {
    }

    void notifyMessageTransmitted() {
    }

    void notifyCharacteristicUpdate(EncodedReceiveMessage message) {
    }

    enum RequestResult {
        SUCCESSFUL,
        RETRY,
        FAILURE
    }

    class BooleanSupplierRetryStrategy {

        Supplier<Boolean> functionToBeExecuted;
        Runnable executedInCaseOfFailure;
        int maximumNumberOfRetries;
        int numberOfRetries = 0;
        DeviceContext context;

        BooleanSupplierRetryStrategy(Supplier<Boolean> theFunctionToBeExecuted, Runnable theFailureFunction,
                int theMaximumNumberOfRetries, DeviceContext theContext) {
            functionToBeExecuted = theFunctionToBeExecuted;
            executedInCaseOfFailure = theFailureFunction;
            maximumNumberOfRetries = theMaximumNumberOfRetries;
            context = theContext;
        }

        void execute() {
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

        RequestResult tryExecute() {
            boolean trialSuccessful = functionToBeExecuted.get();
            if (trialSuccessful) {
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

    class BooleanFunctionRetryStrategy<T> {

        Function<T, Boolean> functionToBeExecuted;
        Runnable executedInCaseOfFailure;
        int maximumNumberOfRetries;
        int numberOfRetries = 0;
        DeviceContext context;

        BooleanFunctionRetryStrategy(Function<T, Boolean> theFunctionToBeExecuted, Runnable theFailureFunction,
                int theMaximumNumberOfRetries, DeviceContext theContext) {
            functionToBeExecuted = theFunctionToBeExecuted;
            executedInCaseOfFailure = theFailureFunction;
            maximumNumberOfRetries = theMaximumNumberOfRetries;
            context = theContext;
        }

        void execute(T arg) {
            RequestResult connectionRequestResult;

            do {
                connectionRequestResult = tryExecute(arg);
            } while (connectionRequestResult == RequestResult.RETRY);

            switch (connectionRequestResult) {
                case FAILURE:
                    executedInCaseOfFailure.run();
                    break;
                default:
                    break;
            }
        }

        RequestResult tryExecute(T arg) {
            boolean trialSuccessful = functionToBeExecuted.apply(arg);
            if (trialSuccessful) {
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
}

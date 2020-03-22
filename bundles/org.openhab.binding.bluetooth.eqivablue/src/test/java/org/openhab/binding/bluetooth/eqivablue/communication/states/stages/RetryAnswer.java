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
package org.openhab.binding.bluetooth.eqivablue.communication.states.stages;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Frank Heister - Initial contribution
 */
class RetryAnswer implements Answer<Boolean> {

    private int numberOfRetries = 0;
    private int maximumNumberOfRetries;

    public RetryAnswer(int theMaximumNumberOfRetries) {
        maximumNumberOfRetries = theMaximumNumberOfRetries;
    }

    @Override
    public Boolean answer(InvocationOnMock invocation) {
        numberOfRetries++;
        if (numberOfRetries <= maximumNumberOfRetries) {
            return false;
        } else {
            return true;
        }
    }
}
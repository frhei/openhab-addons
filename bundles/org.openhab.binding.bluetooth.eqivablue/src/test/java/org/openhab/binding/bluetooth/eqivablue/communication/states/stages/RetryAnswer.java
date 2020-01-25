package org.openhab.binding.bluetooth.eqivablue.communication.states.stages;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

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
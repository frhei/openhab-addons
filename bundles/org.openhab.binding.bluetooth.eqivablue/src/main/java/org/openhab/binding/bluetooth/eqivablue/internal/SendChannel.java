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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.bluetooth.eqivablue.internal.messages.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Frank Heister - Initial contribution
 */
public class SendChannel {
    enum JobCommand {
        Send,
        Suspend,
        Stop
    }

    private final Logger logger = LoggerFactory.getLogger(SendChannel.class);

    private ThermostatContext context;
    private DeviceConnection deviceConnection;
    private BlockingQueue<SendMessage> sendQueue;
    private BlockingQueue<JobCommand> jobCommandQueue;

    public SendChannel(ThermostatContext theThermostatContext, DeviceConnection theDeviceConnection) {
        context = theThermostatContext;
        deviceConnection = theDeviceConnection;
        sendQueue = new LinkedBlockingQueue<SendMessage>();
        jobCommandQueue = new LinkedBlockingQueue<JobCommand>();
        context.startSendJob(() -> sendInLoop());
    }

    public void startSending() {
        logger.debug("{} starts sending messages", context.getName());
        jobCommandQueue.add(JobCommand.Send);
    }

    public void suspendSending() {
        logger.debug("{} suspends sending messages", context.getName());
        jobCommandQueue.add(JobCommand.Suspend);
    }

    public void stopSending() {
        logger.debug("{} stops sending messages", context.getName());
        jobCommandQueue.add(JobCommand.Stop);
    }

    public void send(SendMessage messageToBeSent) {
        sendQueue.add(messageToBeSent);
    }

    private void sendInLoop() {
        boolean keepSending = true;
        logger.debug("{} enters send loop", context.getName());
        do {
            JobCommand command = jobCommandQueue.poll();
            logger.debug("{} got {} command", context.getName(), command);
            switch (command) {
                case Send:
                    processSendQueue();
                    jobCommandQueue.add(JobCommand.Send);
                    break;
                case Stop:
                    keepSending = false;
                    break;
                case Suspend:
                default:
                    break;
            }
        } while (keepSending);
    }

    private void processSendQueue() {
        try {
            processNextMessage();
        } catch (UnsupportedOperationException e) {
            logger.debug("{} could not send message: {}", context.getName(), e.getMessage());
        }
    }

    private void processNextMessage() {
        SendMessage messageToBeSent = null;
        messageToBeSent = determineMessageToBeSentNext();
        deviceConnection.sendMessage(messageToBeSent);
    }

    private SendMessage determineMessageToBeSentNext() {
        SendMessage message = null;
        message = pollNextMessageFromQueue();
        if (message == null) {
            message = getQueryMessageToKeepConnectionUp();
        }
        logger.debug("{}: message {} has been taken from send queue", context.getName(), message);
        return message;
    }

    private SendMessage pollNextMessageFromQueue() {
        SendMessage message = null;
        try {
            message = pollNextMessageFromQueueWithTimeout();
        } catch (InterruptedException e) {
        }
        return message;
    }

    private SendMessage pollNextMessageFromQueueWithTimeout() throws InterruptedException {
        return sendQueue.poll(context.getConnectionKeepupIntervalInMilliseconds(), TimeUnit.MILLISECONDS);
    }

    private SendMessage getQueryMessageToKeepConnectionUp() {
        return SendMessage.queryStatus();
    }

}

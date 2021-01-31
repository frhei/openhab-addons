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
package org.openhab.binding.bluetooth.eqivablue.internal.communication;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.eqivablue.internal.messages.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Frank Heister - Initial contribution
 */
@NonNullByDefault
public class CommandHandler {

    private final Logger logger = LoggerFactory.getLogger(CommandHandler.class);

    private BlockingQueue<SendMessage> commands = new LinkedBlockingQueue<SendMessage>();

    private @Nullable SendMessage messageCurrentlyInProcessing = null;

    public synchronized boolean areCommandsPending() {
        return !commands.isEmpty();
    }

    public synchronized int size() {
        return commands.size() + (messageCurrentlyInProcessing == null ? 0 : 1);
    }

    public @Nullable synchronized SendMessage peekCommand() {
        if (messageCurrentlyInProcessing == null) {
            messageCurrentlyInProcessing = commands.poll();
        }
        return messageCurrentlyInProcessing;
    }

    public synchronized void popCommand() {
        messageCurrentlyInProcessing = null;
    }

    public synchronized void add(SendMessage theMessage) {
        int oldSize = size();
        while (commands.remove(theMessage)) {
        }
        if (!theMessage.equals(messageCurrentlyInProcessing)) {
            commands.add(theMessage);
        }
        logger.debug("CommandHandler adding {}:  size {} -> {}", theMessage, oldSize, size());
    }

}

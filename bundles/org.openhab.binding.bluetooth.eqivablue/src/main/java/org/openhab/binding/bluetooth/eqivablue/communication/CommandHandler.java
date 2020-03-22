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
package org.openhab.binding.bluetooth.eqivablue.communication;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bluetooth.eqivablue.internal.messages.SendMessage;

/**
 * @author Frank Heister - Initial contribution
 */
@NonNullByDefault
public class CommandHandler {

    private BlockingQueue<SendMessage> commands = new LinkedBlockingQueue<SendMessage>();

    public boolean areCommandsPending() {
        return !commands.isEmpty();
    }

    public SendMessage peekCommand() {
        return commands.peek();
    }

    public void popCommand() {
        commands.poll();
    }

    public void add(SendMessage theMessage) {
        commands.add(theMessage);
    }

}

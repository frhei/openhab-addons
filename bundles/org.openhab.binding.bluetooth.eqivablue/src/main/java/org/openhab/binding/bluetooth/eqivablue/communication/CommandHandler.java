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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bluetooth.eqivablue.internal.messages.SendMessage;

/**
 * @author Frank Heister - Initial contribution
 */
@NonNullByDefault
public class CommandHandler {

    public boolean areCommandsPending() {
        // TODO Auto-generated method stub
        return true;
    }

    public SendMessage peekCommand() {
        // TODO Auto-generated method stub
        return SendMessage.queryStatus();
    }

    public void popCommand() {
        // TODO Auto-generated method stub

    }

}

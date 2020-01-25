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

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Frank Heister - Initial contribution
 */
@NonNullByDefault
@Aspect
public class TraceAspect {

    private final Logger logger = LoggerFactory.getLogger(TraceAspect.class);

    @Before(value = "@annotation(Trace)")
    public void before(JoinPoint joinPoint) throws Throwable {
        logger.trace("monitor.before, class: " + joinPoint.getSignature().getDeclaringType().getSimpleName()
                + ", method: " + joinPoint.getSignature().getName());
    }

    @After(value = "@annotation(org.openhab.binding.bluetooth.eqivablue.communication.states.Trace)")
    public void after(JoinPoint joinPoint) throws Throwable {
        logger.debug("monitor.after, class: " + joinPoint.getSignature().getDeclaringType().getSimpleName()
                + ", method: " + joinPoint.getSignature().getName());
    }
}

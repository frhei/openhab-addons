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

import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.openhab.binding.bluetooth.eqivablue.internal.messages.SendMessage;
import org.openhab.binding.bluetooth.eqivablue.internal.messages.SetBoostModeMessage;
import org.openhab.binding.bluetooth.eqivablue.internal.messages.SetEcoAndComfortTemperatureMessage;
import org.openhab.binding.bluetooth.eqivablue.internal.messages.SetOperatingModeModeMessage;
import org.openhab.binding.bluetooth.eqivablue.internal.messages.SetTargetTemperatureMessage;
import org.openhab.binding.bluetooth.eqivablue.internal.messages.SwitchToComfortTemperature;
import org.openhab.binding.bluetooth.eqivablue.internal.messages.SwitchToEcoTemperature;
import org.openhab.binding.bluetooth.eqivablue.internal.messages.UpdateCurrentTimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Frank Heister - Initial contribution
 */
public class SendChannelTest {

    private final Logger logger = LoggerFactory.getLogger(SendChannelTest.class);

    @Mock
    private ThermostatContext context;
    @Mock
    private DeviceConnection connection;

    private static ExecutorService executor;
    private Future<?> sendJob = null;

    private static List<SendMessage> messageTemplates;

    @Rule
    public TestName name = new TestName();

    @BeforeClass
    public static void setUpClass() {
        executor = Executors.newFixedThreadPool(10);
        messageTemplates = new ArrayList<SendMessage>();
        messageTemplates.add(new SetTargetTemperatureMessage(15.0f));
        messageTemplates.add(new SwitchToComfortTemperature());
        messageTemplates.add(new SwitchToEcoTemperature());
        messageTemplates.add(new UpdateCurrentTimeMessage());
        messageTemplates.add(new SetOperatingModeModeMessage(OperatingMode.Scheduled));
        messageTemplates.add(new SetBoostModeMessage(true));
        messageTemplates.add(new SetEcoAndComfortTemperatureMessage(19.0f, 9.5f));
    }

    @Before
    public void setUp() {
        initMocks(this);
        logger.info("executing test: {}", name.getMethodName());
        when(context.getConnectionKeepupIntervalInMilliseconds()).thenReturn(50L);
        when(context.getName()).thenReturn("Test");

        doAnswer((Answer<Void>) invocation -> {
            Runnable task = (Runnable) invocation.getArgument(0);
            sendJob = executor.submit(task);
            logger.debug("{}: startSendJob of context got called", name.getMethodName());
            return null;
        }).when(context).startSendJob(any(Runnable.class));

        doAnswer((Answer<Void>) invocation -> {
            if (sendJob != null) {
                sendJob.cancel(true);
                sendJob = null;
                logger.debug("{}: cancelSendJob of context got called", name.getMethodName());
            }
            return null;
        }).when(context).cancelSendJob();
    }

    @After
    public void tearDown() {
        if (sendJob != null) {
            sendJob.cancel(true);
            sendJob = null;
            logger.debug("{}: SendJob canceled due to tear down", name.getMethodName());
        }
    }

    @Test
    public void given_SendChannelIsEstablished_When_SendingIsNotStarted_Then_NoMessageIsReceived() {

        SendChannel sendChannel = new SendChannel(context, connection);
        sleepForConnectionKeepUpIntervalTimes(3);

        verify(connection, never()).sendMessage(any());
    }

    @Test
    public void given_SendingIsStarted_When_NoMessageIsSent_Then_KeepAliveMessagesAreSent() {

        // given sending is Started
        SendChannel sendChannel = new SendChannel(context, connection);
        sendChannel.startSending();

        // when no message is sent
        sleepForConnectionKeepUpIntervalTimes(3);
        sendChannel.stopSending();

        // then keep-alive messages are sent
        verify(connection, after(context.getConnectionKeepupIntervalInMilliseconds()).times(3))
                .sendMessage(isA(UpdateCurrentTimeMessage.class));
    }

    @Test
    public void given_SendingIsStarted_When_MessageIsSentToChannel_Then_MessageGetsForwardedToConnection() {

        // given sending is Started
        SendChannel sendChannel = new SendChannel(context, connection);
        sendChannel.startSending();

        // when message is sent to channel
        SendMessage message = messageTemplates.get(0);
        sendChannel.send(message);

        // then exactly this message is forwarded to the connection
        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(connection, after(context.getConnectionKeepupIntervalInMilliseconds()))
                .sendMessage(messageCaptor.capture());
        assertSame(message, messageCaptor.getValue());
        verify(connection, times(1)).sendMessage(any(SetTargetTemperatureMessage.class));
        verify(connection, times(1)).sendMessage(any());
        sendChannel.stopSending();

    }

    @Test
    public void given_SendingIsStarted_When_SequenceOfMessagesIsSentToChannel_Then_AllMessagesGetForwardedToConnectionImmediately() {

        int numberOfRandomMessages = 100;
        // given sending is Started
        SendChannel sendChannel = new SendChannel(context, connection);
        sendChannel.startSending();

        // when sequence is sent to channel
        List<SendMessage> randomMessageSequence = getRandomMessageSequenceWithLength(numberOfRandomMessages);
        for (SendMessage message : randomMessageSequence) {
            sendChannel.send(message);
        }

        // then the number of forwarded messages is exactly the same
        verify(connection, after(context.getConnectionKeepupIntervalInMilliseconds()).times(numberOfRandomMessages))
                .sendMessage(any(SendMessage.class));
        sendChannel.stopSending();
    }

    @Test
    public void given_SendingIsStarted_When_SequenceOfMessagesIsSentToChannel_Then_AllMessagesGetForwardedToConnectionInSameOrder() {

        int numberOfRandomMessages = 100;
        // given sending is Started
        SendChannel sendChannel = new SendChannel(context, connection);
        sendChannel.startSending();

        // when sequence is sent to channel
        List<SendMessage> randomMessageSequence = getRandomMessageSequenceWithLength(numberOfRandomMessages);
        for (SendMessage message : randomMessageSequence) {
            sendChannel.send(message);
        }

        // then the order of forwarded messages is exactly the same
        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(connection, after(context.getConnectionKeepupIntervalInMilliseconds()).times(numberOfRandomMessages))
                .sendMessage(messageCaptor.capture());
        // assertEquals(randomMessageSequence, messageCaptor.getAllValues());
        sendChannel.stopSending();
    }

    private void sleepForConnectionKeepUpIntervalTimes(int multiplier) {
        try {
            TimeUnit.MILLISECONDS.sleep(context.getConnectionKeepupIntervalInMilliseconds() * multiplier);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private List<SendMessage> getRandomMessageSequenceWithLength(int length) {
        List<SendMessage> randomMessageSequence = new ArrayList<SendMessage>(length);
        Random randomNumber = new Random();
        int maximumRandomNumber = messageTemplates.size() - 1;
        for (int count = 0; count < length; count++) {
            randomMessageSequence.add(messageTemplates.get(randomNumber.nextInt(maximumRandomNumber)));
        }
        return randomMessageSequence;
    }
}

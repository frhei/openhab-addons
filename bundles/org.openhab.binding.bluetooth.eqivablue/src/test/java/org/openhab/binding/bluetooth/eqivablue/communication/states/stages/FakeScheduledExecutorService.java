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

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Frank Heister - Initial contribution
 */
@NonNullByDefault
abstract class FakeScheduledExecutorService implements ScheduledExecutorService {

    public interface ScheduledFutureListener {
        public void notifyScheduledCommand(ScheduledFuture<?> future);
    }

    public class Job implements ScheduledFuture<Boolean> {

        Runnable scheduledCommand;
        Instant scheduledInstant;
        boolean canceled = false;
        boolean done = false;

        public Job(Runnable command, long delay, TimeUnit unit) {
            scheduledCommand = command;
            if (clock != null) {
                scheduledInstant = clock.instant().plusNanos(unit.toNanos(delay));
            } else {
                scheduledInstant = Instant.MAX;
            }
        }

        @Override
        public long getDelay(@Nullable TimeUnit unit) {
            return (unit != null) ? unit.toNanos(scheduledInstant.getNano()) : 0;
        }

        @Override
        public int compareTo(@Nullable Delayed o) {
            return 0;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            canceled = true;
            return true;
        }

        @Override
        public boolean isCancelled() {
            return canceled;
        }

        @Override
        public boolean isDone() {
            return done;
        }

        @Override
        public Boolean get() throws InterruptedException, ExecutionException {
            return true;
        }

        @Override
        public Boolean get(long timeout, @Nullable TimeUnit unit)
                throws InterruptedException, ExecutionException, TimeoutException {
            return true;
        }

        public boolean ready(Instant now) {
            return !scheduledInstant.isAfter(now) && !isCancelled();
        }

        public void run() {
            scheduledCommand.run();
            done = true;
        }

        public boolean pending(Instant now) {
            return scheduledInstant.isAfter(now) && !isCancelled();
        }

    }

    private List<Job> jobs = new ArrayList<>();
    private @Nullable Clock clock;
    private List<ScheduledFutureListener> scheduledFutureListeners = new ArrayList<>();

    public void setClock(Clock clock) {
        this.clock = clock;
    }

    public void addListener(ScheduledFutureListener theListener) {
        scheduledFutureListeners.add(theListener);
    }

    public void removeListener(ScheduledFutureListener theListener) {
        scheduledFutureListeners.remove(theListener);
    }

    public void notifyListeners(ScheduledFuture<?> future) {
        for (ScheduledFutureListener listener : scheduledFutureListeners) {
            listener.notifyScheduledCommand(future);
        }
    }

    @Override
    public ScheduledFuture<?> schedule(@Nullable Runnable command, long delay, @Nullable TimeUnit unit) {
        Job newJob;
        if (command == null || unit == null) {
            newJob = new Job(() -> {
            }, 0, TimeUnit.NANOSECONDS);
        } else {
            newJob = new Job(command, delay, unit);
        }
        jobs.add(newJob);
        notifyListeners(newJob);
        return newJob;
    }

    void run() {
        if (clock != null) {
            Instant now = clock.instant();
            List<Job> ready = jobs.stream().filter(job -> job.ready(now)).collect(Collectors.toList());
            jobs = jobs.stream().filter(job -> job.pending(now)).collect(Collectors.toCollection(ArrayList::new));
            ready.forEach(Job::run);
        }
    }

    @Override
    public void execute(@Nullable Runnable command) {
        if (command != null) {
            command.run();
        }
    }
}
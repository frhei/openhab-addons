package org.openhab.binding.bluetooth.eqivablue.internal;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class FakeScheduledExecutorService implements ScheduledExecutorService {
    public class Job implements ScheduledFuture<Void> {

        Runnable scheduledCommand;
        Instant scheduledInstant;
        boolean canceled = false;
        boolean done = false;

        public Job(Runnable command, long delay, TimeUnit unit) {
            scheduledCommand = command;
            scheduledInstant = clock.instant().plusNanos(unit.toNanos(delay));
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.toNanos(scheduledInstant.getNano());
        }

        @Override
        public int compareTo(Delayed o) {
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
        public Void get() throws InterruptedException, ExecutionException {
            return null;
        }

        @Override
        public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return null;
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

    private final Logger logger = LoggerFactory.getLogger(FakeScheduledExecutorService.class);

    private List<Job> jobs = new ArrayList<>();
    private Clock clock;
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
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        Job newJob = new Job(command, delay, unit);
        jobs.add(newJob);
        notifyListeners(newJob);
        return newJob;
    }

    void run() {
        Instant now = clock.instant();
        List<Job> ready = jobs.stream().filter(job -> job.ready(now)).collect(Collectors.toList());
        jobs = jobs.stream().filter(job -> job.pending(now)).collect(Collectors.toCollection(ArrayList::new));
        logger.debug("Pending Jobs list length {}", jobs.size());
        logger.debug("Ready Jobs list length {}", ready.size());
        ready.forEach(Job::run);
    }

}
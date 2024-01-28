package com.bawnorton.neruina.thread;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ConditionalRunnable {
    private final Runnable runnable;
    private final ConditionChecker conditionChecker;
    private final ScheduledExecutorService scheduler;

    private ConditionalRunnable(Runnable runnable, ConditionChecker conditionChecker) {
        this.runnable = runnable;
        this.conditionChecker = conditionChecker;
        this.scheduler = Executors.newScheduledThreadPool(5);
    }

    public static void create(Runnable runnable, ConditionChecker conditionChecker) {
        new ConditionalRunnable(runnable, conditionChecker).start();
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::executeIfConditionSucceeds, 0, 10, TimeUnit.MILLISECONDS);
    }

    private void executeIfConditionSucceeds() {
        if (conditionChecker.checkCondition()) {
            scheduler.shutdown();
            runnable.run();
        }
    }

    public interface ConditionChecker {
        boolean checkCondition();
    }
}
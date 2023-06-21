package com.bawnorton.neruina.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public class ConditionalRunnable {
    private final ReentrantLock LOCK = new ReentrantLock();
    private boolean conditionMet = false;

    private static final ExecutorService executor = Executors.newFixedThreadPool(4);

    public static void create(Runnable task, ConditionChecker checker) {
        new ConditionalRunnable().run(task, checker);
    }

    private void run(Runnable task, ConditionChecker checker) {
        executor.execute(() -> {
            try {
                synchronized (LOCK) {
                    while (!conditionMet) {
                        LOCK.wait(1000);
                    }
                    task.run();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        executor.execute(() -> {
            while (!checker.isCompleted()) {
                checker.run();
            }
            synchronized (LOCK) {
                conditionMet = true;
                LOCK.notify();
            }
        });
    }

    // this is ... not the best way to do this, but it works
    @FunctionalInterface
    public interface ConditionChecker extends Runnable {
        AtomicBoolean completed = new AtomicBoolean(false);

        boolean checkCondition();

        default void run() {
            completed.set(checkCondition());
        }

        default boolean isCompleted() {
            return completed.get();
        }
    }
}
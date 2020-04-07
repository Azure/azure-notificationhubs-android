package com.microsoft.windowsazure.messaging.notificationhubs.async;

import java.util.concurrent.Future;

/**
 * Widely compatible Future implementation.
 * @param <T>
 */
public interface NotificationHubFuture<T> extends Future<T> {
    /**
     * Waits if necessary for the computation to complete, and then retrieves its result.
     * @return The computed result.
     */
    T get();

    /**
     * Executes a callback once the computation is completed with the result.
     * The consumer function is called in the U.I. thread.
     * @param function The action to perform upon completion.
     */
    void thenAccept(NotificationHubConsumer<T> function);

    /**
     * Retrieves the whether or not this task has finished executing.
     * @return False if this task is still running, true in all other circumstances.
     */
    boolean isDone();
}

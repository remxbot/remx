package com.remxbot.bot.util;

import java.util.concurrent.locks.Lock;

/**
 * Helper class to automatically lock and unlock
 */
public class ResourceLock implements AutoCloseable {
    private Lock lock;

    /**
     * @param lock the lock to lock.
     */
    public ResourceLock(Lock lock) {
        this.lock = lock;
        lock.lock();
    }

    @Override
    public void close() {
        lock.unlock();
    }
}

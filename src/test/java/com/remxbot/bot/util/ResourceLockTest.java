package com.remxbot.bot.util;

import org.junit.Test;

import java.util.concurrent.locks.Lock;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ResourceLockTest {
    @Test
    public void lockTest() {
        Lock lock = mock(Lock.class);
        //noinspection EmptyTryBlock
        try (var l = new ResourceLock(lock)) {}
        verify(lock).lock();
        verify(lock).unlock();
    }
}
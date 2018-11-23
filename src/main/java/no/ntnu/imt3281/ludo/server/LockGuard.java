package no.ntnu.imt3281.ludo.server;

import no.ntnu.imt3281.ludo.common.Logger;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Implements a C++ like LockGuard see: https://en.cppreference.com/w/cpp/thread/lock_guard,
 * I.e. a lock that unlocks itself in the destructor,
 * or to adapt to the Java case, it unlocks itself on close.
 * So, as long as you use try with resources and create the LockGuard there, you cannot forget to unlock the lock.
 */
public class LockGuard implements AutoCloseable {
//    static private AtomicInteger sWaitingThreads = new AtomicInteger(-1);
//    static private int sCurrentLocksTaken = 0;

    private ReentrantLock mLock;

    public LockGuard(ReentrantLock lock) {
        mLock = lock;

//        var threadID = Thread.currentThread().getId();
//        if (!mLock.isHeldByCurrentThread()) {
//            var waiting = sWaitingThreads.incrementAndGet();
//            Logger.log(Logger.Level.DEBUG, "%d Acquiring lock, now %d are waiting", threadID, waiting);
//            dumpStackWithID();
//        }
        mLock.lock();
//        sCurrentLocksTaken++;
//        Logger.log(Logger.Level.DEBUG, "%d Locking, has taken: %d", threadID, sCurrentLocksTaken);
//        dumpStackWithID();
    }

    @Override
    public void close() {
//        sCurrentLocksTaken--;
//        var threadID = Thread.currentThread().getId();
//        Logger.log(Logger.Level.DEBUG, "%d Unlocking, has taken: %d", threadID, sCurrentLocksTaken);
//        if (mLock.isHeldByCurrentThread() && sCurrentLocksTaken == 0) {
//            var waiting = sWaitingThreads.decrementAndGet();
//            Logger.log(Logger.Level.DEBUG, "%d Released lock, now %d are waiting", threadID, waiting);
//        }
        mLock.unlock();
    }

    private void dumpStackWithID() {
        var stackTraces = Thread.getAllStackTraces();
        var trace = stackTraces.get(Thread.currentThread());
        //Arrays.stream(trace).
        String s = Arrays.stream(trace).map(Object::toString).collect(Collectors.joining("\n"));
        //Arrays.stream(trace).
        Logger.log(Logger.Level.DEBUG, "Thread ID: %d %s", Thread.currentThread().getId(), s);

    }
}

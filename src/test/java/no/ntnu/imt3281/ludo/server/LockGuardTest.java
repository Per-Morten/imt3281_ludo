package no.ntnu.imt3281.ludo.server;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.concurrent.locks.ReentrantLock;

import static org.mockito.Mockito.mock;

/**
 * Checks if the LockGuard actually unlocks on "close"
 * as it is quite annoying to find out that it does not happen.
 */
public class LockGuardTest {

    @Test
    public void unlocksOnClose() {
        var mockLock = mock(ReentrantLock.class);

        try (var lock = new LockGuard(mockLock)) {

        }

        Mockito.verify(mockLock, Mockito.times(1)).unlock();
    }

    @Test
    public void unlocksOnException() {
        var mockLock = mock(ReentrantLock.class);

        try {
            try (var lock = new LockGuard(mockLock)) {
                throw new RuntimeException("Some Exception");
            }
        } catch (RuntimeException e) {

        }

        Mockito.verify(mockLock, Mockito.times(1)).unlock();
    }
}

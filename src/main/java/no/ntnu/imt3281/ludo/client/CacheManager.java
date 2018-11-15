package no.ntnu.imt3281.ludo.client;

import javafx.application.Platform;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Manage state in a thread safe manner.
 */
public class CacheManager {

    private ArrayBlockingQueue<Cache> mCache = new ArrayBlockingQueue<Cache>(1);

    public CacheManager(Cache initialCache) {
        try {
            mCache.put(initialCache);
        } catch (InterruptedException e) {
            Platform.exit();
        }
    }

    /**
     * Full deep copy of the current state. Blocking in case ongoing mutation
     *
     * @return state object
     */
    public Cache copy() {
        Cache copy = new Cache();
        try {
            Cache cache = mCache.take();
            copy = Cache.deepCopy(cache);
            mCache.put(cache);
        } catch (InterruptedException e) {
            Platform.exit();
        }
        return copy;
    }

    /**
     * Mutate the state with provided mutation. Block all incoming copies until
     * mutation is completed.
     *
     * @param mutation which should be applied on state
     */
    public void commit(Mutation mutation) {
        try {
            Cache cache = mCache.take();
            mutation.run(cache);
            mCache.put(cache);
        } catch (InterruptedException e) {
            Platform.exit();
        }
    }
}

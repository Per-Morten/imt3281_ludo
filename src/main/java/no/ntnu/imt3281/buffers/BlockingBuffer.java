package no.ntnu.imt3281.buffers;

import java.util.concurrent.ArrayBlockingQueue;

public class BlockingBuffer<T> implements Buffer<T> {
    private final ArrayBlockingQueue<T> buffer;
    
    public BlockingBuffer() {
        buffer = new ArrayBlockingQueue<>(100);
    }
    
    @Override
    public void put(T value) throws InterruptedException {
        buffer.put(value);
    }

    @Override
    public T get() throws InterruptedException {
        T value = buffer.take();
        return value;
    }
}

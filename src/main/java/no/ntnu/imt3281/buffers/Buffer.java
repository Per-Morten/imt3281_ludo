package no.ntnu.imt3281.buffers;

public interface Buffer <T>{
    public void put(T value) throws InterruptedException;
    public T get() throws InterruptedException;
}

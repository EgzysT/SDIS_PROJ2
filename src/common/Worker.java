package common;

/**
 * Worker's abstraction
 */
public abstract class Worker implements Runnable {

    @Override
    public void run() {
        work();
    }

    /**
     * Work to be done
     */
    protected abstract void work();
}

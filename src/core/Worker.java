package core;

public abstract class Worker implements Runnable {

    @Override
    public void run() {
        work();
    }

    protected abstract void work();
}

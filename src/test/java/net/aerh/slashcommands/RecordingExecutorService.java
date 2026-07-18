package net.aerh.slashcommands;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Test executor that records submitted tasks instead of running them, so a test can assert whether
 * an interaction was dispatched off the event thread without depending on real threading.
 */
class RecordingExecutorService extends AbstractExecutorService {

    private final List<Runnable> tasks = new ArrayList<>();
    private boolean shutdown = false;

    @Override
    public void execute(Runnable command) {
        tasks.add(command);
    }

    int taskCount() {
        return tasks.size();
    }

    void runAll() {
        for (Runnable task : tasks) {
            task.run();
        }
    }

    @Override
    public void shutdown() {
        shutdown = true;
    }

    @Override
    public List<Runnable> shutdownNow() {
        shutdown = true;
        return new ArrayList<>(tasks);
    }

    @Override
    public boolean isShutdown() {
        return shutdown;
    }

    @Override
    public boolean isTerminated() {
        return shutdown;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) {
        return true;
    }
}

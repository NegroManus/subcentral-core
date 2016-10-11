package de.subcentral.fx.ctrl;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.subcentral.fx.FxUtil;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.concurrent.WorkerStateEvent;
import javafx.scene.control.Alert;
import javafx.stage.Window;

public class TaskExecutor implements ExecutorService {
    private static final Logger   log = LogManager.getLogger(MainController.class);

    private final ExecutorService executor;
    private final Window          owner;

    public TaskExecutor(ExecutorService executor, Window owner) {
        this.executor = Objects.requireNonNull(executor, "executor");
        this.owner = owner;
    }

    public Window getOwner() {
        return owner;
    }

    @Override
    public void execute(Runnable command) {
        observeTask(command);
        executor.execute(command);
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return executor.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return executor.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return executor.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return executor.awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        observeTask(task);
        return executor.submit(task);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        observeTask(task);
        return executor.submit(task, result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        observeTask(task);
        return executor.submit(task);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        for (Callable<T> task : tasks) {
            observeTask(task);
        }
        return executor.invokeAll(tasks);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        for (Callable<T> task : tasks) {
            observeTask(task);
        }
        return executor.invokeAll(tasks, timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        for (Callable<T> task : tasks) {
            observeTask(task);
        }
        return executor.invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        for (Callable<T> task : tasks) {
            observeTask(task);
        }
        return executor.invokeAny(tasks, timeout, unit);
    }

    private void observeTask(Runnable task) {
        if (task instanceof Task<?>) {
            observeTask((Task<?>) task);
        }
    }

    private void observeTask(Callable<?> task) {
        if (task instanceof Task<?>) {
            observeTask((Task<?>) task);
        }
    }

    protected void observeTask(Task<?> task) {
        // task.setOnRunning((WorkerStateEvent evt) ->
        // {
        // log.debug("Executing background task '" + task.getTitle() + "'");
        // });
        // task.setOnSucceeded((WorkerStateEvent evt) ->
        // {
        // log.debug("Successfully executed background task '" + task.getTitle() + "'");
        // });
        task.setOnFailed((WorkerStateEvent evt) -> {
            Worker<?> worker = evt.getSource();
            StringBuilder sb = new StringBuilder();
            sb.append("Execution of ");
            if (!worker.getTitle().isEmpty()) {
                sb.append("the background task '");
                sb.append(worker.getTitle());
                sb.append("'");
            }
            else {
                sb.append("a background task");
            }
            sb.append(" failed");
            String msg = sb.toString();

            log.error(msg, worker.getException());
            Alert alert = FxUtil.createExceptionAlert(owner, "Task execution failed", msg, worker.getException());
            alert.show();
        });
    }
}

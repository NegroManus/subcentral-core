package de.subcentral.watcher.controller.processing;

import java.util.Objects;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.concurrent.Worker;

public class WorkerStatus extends ObjectBinding<WorkerStatus> {
    private final ReadOnlyProperty<Worker.State> state;
    private final ReadOnlyStringProperty         message;
    private final ReadOnlyProperty<Throwable>    exception;

    // package private
    WorkerStatus(ReadOnlyProperty<Worker.State> state, ReadOnlyStringProperty message, ReadOnlyProperty<Throwable> exception) {
        this.state = Objects.requireNonNull(state, "state");
        this.message = Objects.requireNonNull(message, "message");
        this.exception = Objects.requireNonNull(exception, "exception");
        super.bind(state, message, exception);
    }

    public Worker.State getState() {
        return state.getValue();
    }

    public String getMessage() {
        return message.get();
    }

    public Throwable getException() {
        return exception.getValue();
    }

    @Override
    protected WorkerStatus computeValue() {
        return this;
    }
}

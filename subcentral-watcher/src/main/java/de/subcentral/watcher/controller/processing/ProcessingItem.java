package de.subcentral.watcher.controller.processing;

import java.nio.file.Path;

import javafx.beans.binding.Binding;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.concurrent.Worker;

public interface ProcessingItem {
	ReadOnlyStringProperty nameProperty();

	default String getName() {
		return nameProperty().get();
	}

	ListProperty<Path> getFiles();

	ReadOnlyProperty<Worker.State> stateProperty();

	default Worker.State getState() {
		return stateProperty().getValue();
	}

	ReadOnlyStringProperty messageProperty();

	default String getMessage() {
		return messageProperty().get();
	}

	ReadOnlyDoubleProperty progressProperty();

	default double getProgress() {
		return progressProperty().get();
	}

	ReadOnlyProperty<ProcessingInfo> infoProperty();

	default ProcessingInfo getInfo() {
		return infoProperty().getValue();
	}

	ReadOnlyProperty<Throwable> exceptionProperty();

	default Throwable getException() {
		return exceptionProperty().getValue();
	}

	Binding<WorkerStatus> statusBinding();

	default WorkerStatus getStatus() {
		return statusBinding().getValue();
	}
}

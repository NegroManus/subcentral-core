package de.subcentral.watcher.controller.processing;

import java.nio.file.Path;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.ReadOnlyStringProperty;

public interface ProcessingItem
{
	ReadOnlyStringProperty nameProperty();

	default String getName()
	{
		return nameProperty().get();
	}

	ListProperty<Path> getFiles();

	ReadOnlyStringProperty statusProperty();

	default String getStatus()
	{
		return statusProperty().get();
	}

	ReadOnlyDoubleProperty progressProperty();

	default double getProgress()
	{
		return progressProperty().get();
	}

	ReadOnlyProperty<ProcessingInfo> infoProperty();

	default ProcessingInfo getInfo()
	{
		return infoProperty().getValue();
	}

	ReadOnlyProperty<Throwable> exceptionProperty();

	default Throwable getException()
	{
		return exceptionProperty().getValue();
	}
}

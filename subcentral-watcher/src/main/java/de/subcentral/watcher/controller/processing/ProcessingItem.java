package de.subcentral.watcher.controller.processing;

import java.nio.file.Path;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import de.subcentral.watcher.model.ObservableNamedBeanWrapper;

public interface ProcessingItem
{
	ReadOnlyProperty<ObservableNamedBeanWrapper<?>> beanWrapperProperty();

	ObservableNamedBeanWrapper<?> getBeanWrapper();

	StringBinding nameBinding();

	String getName();

	ListProperty<Path> getFiles();

	ReadOnlyStringProperty statusProperty();

	String getStatus();

	ReadOnlyDoubleProperty progressProperty();

	double getProgress();

	ReadOnlyStringProperty infoProperty();

	String getInfo();
}

package de.subcentral.watcher.controller.processing;

import java.nio.file.Path;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;

public interface ProcessingItem
{
    StringBinding nameBinding();

    String getName();

    ListProperty<Path> getFiles();

    ReadOnlyStringProperty statusProperty();

    String getStatus();

    ReadOnlyDoubleProperty progressProperty();

    double getProgress();

    StringBinding infoBinding();

    String getInfo();
}

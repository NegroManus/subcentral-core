package de.subcentral.watcher.controller.processing;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.subcentral.core.metadata.release.CompatibilityService.CompatibilityInfo;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.StandardRelease;
import de.subcentral.core.naming.SubtitleAdjustmentNamer;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;

public class ProcessingResult implements ProcessingItem
{
    public static enum Method
    {
	DATABASE, GUESSING, COMPATIBILITY
    }

    private final ProcessingTask	   task;
    private final Release		   release;
    private final StringProperty	   name;
    private final ListProperty<Path>	   files    = new SimpleListProperty<>(this, "files", FXCollections.observableArrayList());
    private final StringProperty	   status   = new SimpleStringProperty(this, "status");
    private final DoubleProperty	   progress = new SimpleDoubleProperty(this, "progress");
    private final Property<ProcessingInfo> info	    = new SimpleObjectProperty<>(this, "info");

    /**
     * package protected
     */
    ProcessingResult(ProcessingTask task, Release release)
    {
	this.task = Objects.requireNonNull(task, "task");
	this.release = Objects.requireNonNull(release, "release");

	this.name = new SimpleStringProperty(this, "name", generateName(release));
    }

    private String generateName(Release rls)
    {
	Map<String, Object> effectiveParams = new HashMap<>();
	effectiveParams.putAll(task.getConfig().getNamingParameters());
	effectiveParams.put(SubtitleAdjustmentNamer.PARAM_RELEASE, rls);
	return task.getController().getNamingService().name(task.getTargetObject(), effectiveParams);
    }

    public ProcessingTask getTask()
    {
	return task;
    }

    public Release getRelease()
    {
	return release;
    }

    @Override
    public ReadOnlyStringProperty nameProperty()
    {
	return name;
    }

    @Override
    public ListProperty<Path> getFiles()
    {
	return files;
    }

    void addFile(Path file)
    {
	Platform.runLater(() -> {
	    files.add(file);
	});
    }

    void removeFile(Path file)
    {
	Platform.runLater(() -> {
	    files.remove(file);
	});
    }

    @Override
    public ReadOnlyStringProperty statusProperty()
    {
	return status;
    }

    void updateStatus(final String status)
    {
	Platform.runLater(() -> ProcessingResult.this.status.set(status));
    }

    @Override
    public ReadOnlyDoubleProperty progressProperty()
    {
	return progress;
    }

    void updateProgress(final double progress)
    {
	Platform.runLater(() -> ProcessingResult.this.progress.set(progress));
    }

    @Override
    public ReadOnlyProperty<ProcessingInfo> infoProperty()
    {
	return info;
    }

    void updateInfo(final ProcessingResultInfo info)
    {
	Platform.runLater(() -> ProcessingResult.this.info.setValue(info));
    }

    public static interface MethodInfo
    {
	Method getMethod();
    }

    public static class DatabaseMethodInfo implements MethodInfo
    {
	@Override
	public Method getMethod()
	{
	    return Method.DATABASE;
	}
    }

    public static class GuessingMethodInfo implements MethodInfo
    {
	private final StandardRelease standardRelease;

	GuessingMethodInfo(StandardRelease standardRelease)
	{
	    this.standardRelease = standardRelease;
	}

	@Override
	public Method getMethod()
	{
	    return Method.GUESSING;
	}

	public StandardRelease getStandardRelease()
	{
	    return standardRelease;
	}
    }

    public static class CompatibilityMethodInfo implements MethodInfo
    {
	private final CompatibilityInfo compatibilityInfo;

	CompatibilityMethodInfo(CompatibilityInfo compatibilityInfo)
	{
	    this.compatibilityInfo = compatibilityInfo;
	}

	@Override
	public Method getMethod()
	{
	    return Method.COMPATIBILITY;
	}

	public CompatibilityInfo getCompatibilityInfo()
	{
	    return compatibilityInfo;
	}
    }
}
package de.subcentral.watcher.controller.processing;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import de.subcentral.core.naming.NamingService;

public abstract class AbstractProcessingItem implements ProcessingItem
{
	protected final NamingService		namingService;
	protected final Map<String, Object>	namingParameters;

	protected final ListProperty<Path>	files		= new SimpleListProperty<>(this, "files", FXCollections.observableArrayList());
	protected final StringProperty		status		= new SimpleStringProperty(this, "status", "");
	protected final DoubleProperty		progress	= new SimpleDoubleProperty(this, "progress");

	public AbstractProcessingItem(NamingService namingService, Map<String, Object> namingParameters)
	{
		this.namingService = Objects.requireNonNull(namingService, "namingService");
		this.namingParameters = Objects.requireNonNull(namingParameters, "namingParameters");
	}

	@Override
	public String getName()
	{
		return nameBinding().get();
	}

	@Override
	public ListProperty<Path> getFiles()
	{
		return files;
	}

	@Override
	public ReadOnlyStringProperty statusProperty()
	{
		return status;
	}

	@Override
	public String getStatus()
	{
		return status.get();
	}

	public void updateStatus(String newStatus)
	{
		if (Platform.isFxApplicationThread())
		{
			status.setValue(newStatus);
		}
		else
		{
			Platform.runLater(() -> status.setValue(newStatus));
		}
	}

	@Override
	public ReadOnlyDoubleProperty progressProperty()
	{
		return progress;
	}

	@Override
	public double getProgress()
	{
		return progress.doubleValue();
	}

	public void updateProgress(double newProgress)
	{
		if (Platform.isFxApplicationThread())
		{
			progress.setValue(newProgress);
		}
		else
		{
			Platform.runLater(() -> progress.setValue(newProgress));
		}
	}

	@Override
	public String getInfo()
	{
		return infoBinding().get();
	}
}

package de.subcentral.watcher.controller.processing;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import de.subcentral.watcher.model.ObservableNamedBeanWrapper;

public abstract class AbstractProcessingItem implements ProcessingItem
{
	protected final Property<ObservableNamedBeanWrapper<?>>	beanWrapper	= new SimpleObjectProperty<>(this, "beanWrapper");
	protected final StringProperty							status		= new SimpleStringProperty(this, "status");
	protected final DoubleProperty							progress	= new SimpleDoubleProperty(this, "progress");
	protected final StringProperty							info		= new SimpleStringProperty(this, "info");

	@Override
	public ReadOnlyProperty<ObservableNamedBeanWrapper<?>> beanWrapperProperty()
	{
		return beanWrapper;
	}

	@Override
	public ObservableNamedBeanWrapper<?> getBeanWrapper()
	{
		return beanWrapper.getValue();
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
	public ReadOnlyStringProperty infoProperty()
	{
		return info;
	}

	@Override
	public String getInfo()
	{
		return info.get();
	}

	public void updateInfo(String newInfo)
	{
		if (Platform.isFxApplicationThread())
		{
			info.setValue(newInfo);
		}
		else
		{
			Platform.runLater(() -> info.setValue(newInfo));
		}
	}
}

package de.subcentral.watcher.controller.processing;

import java.nio.file.Path;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import de.subcentral.watcher.model.ObservableNamedBeanWrapper;

public class SourceProcessingItem extends AbstractProcessingItem
{
	private final ListProperty<Path>	sourceFile;

	public SourceProcessingItem(Path srcFile, ObservableNamedBeanWrapper<?> beanWrapper)
	{
		this.sourceFile = new SimpleListProperty<Path>(this, "files", FXCollections.singletonObservableList(srcFile)); // includes null check
		this.beanWrapper.setValue(beanWrapper);
	}

	@Override
	public StringBinding nameBinding()
	{
		return beanWrapper.getValue().nameBinding();
	}

	@Override
	public String getName()
	{
		return nameBinding().get();
	}

	@Override
	public ListProperty<Path> getFiles()
	{
		return sourceFile;
	}
}

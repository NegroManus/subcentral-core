package de.subcentral.watcher.controller.processing;

import java.nio.file.Path;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import de.subcentral.watcher.model.ObservableNamedBeanWrapper;

public class SourceProcessingItem extends AbstractProcessingItem
{
	private final ListProperty<Path>	sourceFiles;

	public SourceProcessingItem(Path srcFile, ObservableNamedBeanWrapper<?> beanWrapper)
	{
		ObservableList<Path> list = FXCollections.observableArrayList(srcFile);
		this.sourceFiles = new SimpleListProperty<Path>(this, "files", list); // includes null check
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
		return sourceFiles;
	}
}

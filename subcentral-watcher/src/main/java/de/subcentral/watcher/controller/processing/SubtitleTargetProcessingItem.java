package de.subcentral.watcher.controller.processing;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.subtitle.SubtitleAdjustment;
import de.subcentral.core.naming.SubtitleAdjustmentNamer;
import de.subcentral.watcher.model.ObservableNamableBeanWrapper;

public class SubtitleTargetProcessingItem extends AbstractProcessingItem
{
	private final Release				release;
	private final ListProperty<Path>	files	= new SimpleListProperty<>(this, "files", FXCollections.observableArrayList());
	private final StringBinding			nameBinding;

	public SubtitleTargetProcessingItem(ObservableNamableBeanWrapper<SubtitleAdjustment> sub, Release release)
	{
		this.beanWrapper.setValue(sub);
		this.release = release;
		nameBinding = new StringBinding()
		{
			{
				super.bind(SubtitleTargetProcessingItem.this.beanWrapper.getValue());
			}

			@Override
			protected String computeValue()
			{
				// Combine general naming params with the param for naming the associated release
				Map<String, Object> params = new HashMap<>(beanWrapper.getValue().getNamingParameters());
				params.put(SubtitleAdjustmentNamer.PARAM_RELEASE, SubtitleTargetProcessingItem.this.release);
				return beanWrapper.getValue().getNamingService().name(beanWrapper.getValue().getBean(), params);
			}
		};
	}

	@Override
	public StringBinding nameBinding()
	{
		return nameBinding;
	}

	@Override
	public String getName()
	{
		return nameBinding.get();
	}

	@Override
	public ListProperty<Path> getFiles()
	{
		return files;
	}

}

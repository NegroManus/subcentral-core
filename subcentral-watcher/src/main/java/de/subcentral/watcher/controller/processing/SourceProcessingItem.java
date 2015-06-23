package de.subcentral.watcher.controller.processing;

import java.nio.file.Path;
import java.util.Map;
import java.util.StringJoiner;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import de.subcentral.core.metadata.subtitle.SubtitleAdjustment;
import de.subcentral.core.naming.NamingService;
import de.subcentral.fx.FxUtil;

public class SourceProcessingItem extends AbstractProcessingItem
{
	private final Property<SubtitleAdjustment>	parsedBean	= new SimpleObjectProperty<SubtitleAdjustment>(this, "parsedBean", null);
	private final StringBinding					nameBinding;

	public SourceProcessingItem(NamingService namingService, Map<String, Object> namingParameters)
	{
		super(namingService, namingParameters);

		nameBinding = new StringBinding()
		{
			{
				super.bind(files);
			}

			@Override
			protected String computeValue()
			{
				StringJoiner s = new StringJoiner(", ");
				for (Path file : files)
				{
					s.add(file.getFileName().toString());
				}
				return s.toString();
			}
		};
	}

	@Override
	public StringBinding nameBinding()
	{
		return nameBinding;
	}

	public Property<SubtitleAdjustment> parsedBeanProperty()
	{
		return parsedBean;
	}

	public SubtitleAdjustment getParsedBean()
	{
		return parsedBean.getValue();
	}

	public void setParsedBean(SubtitleAdjustment parsedBean)
	{
		this.parsedBean.setValue(parsedBean);
	}

	@Override
	public StringBinding infoBinding()
	{
		return FxUtil.createConstantStringBinding("");
	}
}

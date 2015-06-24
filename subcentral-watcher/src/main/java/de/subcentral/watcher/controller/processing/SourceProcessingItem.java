package de.subcentral.watcher.controller.processing;

import java.nio.file.Path;
import java.util.Map;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import de.subcentral.core.metadata.subtitle.SubtitleAdjustment;
import de.subcentral.core.naming.NamingService;
import de.subcentral.fx.FxUtil;

public class SourceProcessingItem extends AbstractProcessingItem
{
	private final ListProperty<Path>			files;
	private final StringBinding					nameBinding;
	private final StringBinding					infoBinding;
	private final Property<SubtitleAdjustment>	parsedBean			= new SimpleObjectProperty<SubtitleAdjustment>(this, "parsedBean", null);
	private final BooleanProperty				sourceFileExists	= new SimpleBooleanProperty(this, "sourceFileExists", true);

	public SourceProcessingItem(NamingService namingService, Map<String, Object> namingParameters, Path sourceFile)
	{
		super(namingService, namingParameters);
		nameBinding = FxUtil.constantStringBinding(sourceFile.getFileName().toString());
		infoBinding = createInfoBinding();
		files = new SimpleListProperty<>(this, "files", FXCollections.singletonObservableList(sourceFile));
	}

	private StringBinding createInfoBinding()
	{
		return new StringBinding()
		{
			{
				super.bind(sourceFileExists);
			}

			@Override
			protected String computeValue()
			{
				return sourceFileExists.get() ? "" : "Source file was deleted";
			}
		};
	}

	@Override
	public ListProperty<Path> getFiles()
	{
		return files;
	}

	@Override
	public StringBinding nameBinding()
	{
		return nameBinding;
	}

	@Override
	public StringBinding infoBinding()
	{
		return infoBinding;
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

	public final BooleanProperty sourceFileExistsProperty()
	{
		return this.sourceFileExists;
	}

	public final boolean isSourceFileExists()
	{
		return this.sourceFileExistsProperty().get();
	}

	public final void setSourceFileExists(final boolean sourceFileExists)
	{
		this.sourceFileExistsProperty().set(sourceFileExists);
	}

}

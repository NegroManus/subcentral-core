package de.subcentral.watcher.settings;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import de.subcentral.core.parsing.ParsingService;

public class ParsingServiceSettingEntry implements SettingEntry<ParsingService>
{
	private final ParsingService	parsingService;
	private final BooleanProperty	enabled;

	public ParsingServiceSettingEntry(ParsingService parsingService, boolean enabled)
	{
		this.parsingService = parsingService;
		this.enabled = new SimpleBooleanProperty(this, "enabled", enabled);
	}

	@Override
	public ParsingService getValue()
	{
		return parsingService;
	}

	@Override
	public final BooleanProperty enabledProperty()
	{
		return this.enabled;
	}

	@Override
	public final boolean isEnabled()
	{
		return this.enabledProperty().get();
	}

	@Override
	public final void setEnabled(final boolean enabled)
	{
		this.enabledProperty().set(enabled);
	}
}
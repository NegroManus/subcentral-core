package de.subcentral.watcher.settings;

import de.subcentral.core.parsing.ParsingService;

public class ParsingServiceSettingEntry extends AbstractSettingEntry<ParsingService>
{
    public ParsingServiceSettingEntry(ParsingService parsingService, boolean enabled)
    {
	super(parsingService, enabled);
    }
}
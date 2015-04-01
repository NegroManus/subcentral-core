package de.subcentral.watcher.settings;

import javafx.beans.binding.StringBinding;

import com.google.common.base.Joiner;

import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.standardizing.ReleaseTagsStandardizer;
import de.subcentral.core.standardizing.TagsReplacer;
import de.subcentral.fx.FXUtil;

public class ReleaseTagsStandardizerSettingEntry extends StandardizerSettingEntry<Release, ReleaseTagsStandardizer>
{
	private static final StringBinding	standardizerTypeAsString	= FXUtil.createConstantStringBinding("Release tags");
	private final StringBinding			ruleAsString;

	public ReleaseTagsStandardizerSettingEntry(ReleaseTagsStandardizer standardizer, boolean enabled)
	{
		super(Release.class, standardizer, enabled);
		ruleAsString = FXUtil.createConstantStringBinding(operationToString(standardizer));
	}

	@Override
	public StringBinding standardizerTypeAsStringBinding()
	{
		return standardizerTypeAsString;
	}

	@Override
	public StringBinding ruleAsStringBinding()
	{
		return ruleAsString;
	}

	public static String getStandardizerTypeString()
	{
		return standardizerTypeAsString.get();
	}

	private static String operationToString(ReleaseTagsStandardizer standardizer)
	{
		TagsReplacer replacer = standardizer.getReplacer();
		StringBuilder sb = new StringBuilder();
		sb.append("If tags ");
		switch (replacer.getQueryMode())
		{
			case CONTAIN:
				sb.append("contain ");
				break;
			case EQUAL:
				sb.append("equal ");
				break;
		}
		Joiner.on(", ").appendTo(sb, replacer.getQueryTags());
		if (replacer.getIgnoreOrder())
		{
			sb.append(" (in any order)");
		}
		sb.append(", then ");
		switch (replacer.getReplaceMode())
		{
			case MATCHED_SEQUENCE:
				sb.append("replace that with ");
				break;
			case COMPLETE_LIST:
				sb.append("set the release's tags to ");
				break;
		}
		Joiner.on(", ").appendTo(sb, replacer.getReplacement());
		return sb.toString();
	}
}
package de.subcentral.watcher.settings;

import javafx.beans.binding.StringBinding;

import com.google.common.base.Joiner;

import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.standardizing.ReleaseTagsStandardizer;
import de.subcentral.core.standardizing.TagsReplacer;
import de.subcentral.fx.FxUtil;

public class ReleaseTagsStandardizerSettingEntry extends StandardizerSettingEntry<Release, ReleaseTagsStandardizer>
{
	private static final StringBinding	standardizerTypeAsString	= FxUtil.createConstantStringBinding("Release tags");
	private final StringBinding			ruleAsString;

	public ReleaseTagsStandardizerSettingEntry(ReleaseTagsStandardizer standardizer, boolean enabled)
	{
		super(Release.class, standardizer, enabled);
		ruleAsString = FxUtil.createConstantStringBinding(operationToString(standardizer));
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
		sb.append('{');
		Joiner.on(", ").appendTo(sb, replacer.getQueryTags());
		sb.append('}');
		if (replacer.getIgnoreOrder())
		{
			sb.append(" (in any order)");
		}
		sb.append(", then ");
		switch (replacer.getReplaceMode())
		{
			case MATCHED_SEQUENCE:
				sb.append("replace it with ");
				break;
			case COMPLETE_LIST:
				sb.append("set the tags to ");
				break;
		}
		sb.append('{');
		Joiner.on(", ").appendTo(sb, replacer.getReplacement());
		sb.append('}');
		return sb.toString();
	}
}
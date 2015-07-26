package de.subcentral.watcher.settings;

import com.google.common.base.Joiner;

import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.standardizing.ReleaseTagsStandardizer;
import de.subcentral.core.standardizing.TagsReplacer;
import de.subcentral.fx.FxUtil;
import javafx.beans.binding.StringBinding;

public class ReleaseTagsStandardizerSettingEntry extends StandardizerSettingEntry<Release, ReleaseTagsStandardizer>
{
	private static final StringBinding	standardizerType	= FxUtil.constantStringBinding("Release tags");
	private final StringBinding			rule;

	public ReleaseTagsStandardizerSettingEntry(ReleaseTagsStandardizer standardizer, boolean beforeQuerying, boolean afterQuerying)
	{
		super(Release.class, standardizer, beforeQuerying, afterQuerying);
		rule = FxUtil.constantStringBinding(formatRule(standardizer));
	}

	@Override
	public StringBinding standardizerTypeStringBinding()
	{
		return standardizerType;
	}

	@Override
	public StringBinding ruleStringBinding()
	{
		return rule;
	}

	public static String getStandardizerTypeString()
	{
		return standardizerType.get();
	}

	private static String formatRule(ReleaseTagsStandardizer standardizer)
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
			sb.append("replace those with ");
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
package de.subcentral.watcher.settings;

import javafx.beans.binding.StringBinding;

import com.google.common.base.Joiner;

import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.standardizing.ReleaseTagsStandardizer;
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
		StringBuilder sb = new StringBuilder();
		sb.append("If tags ");
		switch (standardizer.getQueryMode())
		{
			case CONTAIN:
				sb.append("contain ");
				break;
			case EQUAL:
				sb.append("equal ");
				break;
		}
		Joiner.on(", ").appendTo(sb, standardizer.getQueryTags());
		if (standardizer.getIgnoreOrder())
		{
			sb.append(" (in any order)");
		}
		sb.append(", then ");
		switch (standardizer.getReplaceMode())
		{
			case MATCHED_SEQUENCE:
				sb.append("replace that with ");
				break;
			case COMPLETE_LIST:
				sb.append("set the release's tags to ");
				break;
		}
		Joiner.on(", ").appendTo(sb, standardizer.getReplacement());
		return sb.toString();
	}
}
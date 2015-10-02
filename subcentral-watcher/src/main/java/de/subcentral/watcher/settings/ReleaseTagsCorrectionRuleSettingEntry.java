package de.subcentral.watcher.settings;

import com.google.common.base.Joiner;

import de.subcentral.core.correction.ReleaseTagsCorrector;
import de.subcentral.core.correction.TagsReplacer;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.fx.FxUtil;
import javafx.beans.binding.StringBinding;

public class ReleaseTagsCorrectionRuleSettingEntry extends CorrectionRuleSettingEntry<Release, ReleaseTagsCorrector>
{
	private static final StringBinding	ruleType	= FxUtil.constantStringBinding("Release tags");
	private final StringBinding			rule;

	public ReleaseTagsCorrectionRuleSettingEntry(ReleaseTagsCorrector corrector, boolean beforeQuerying, boolean afterQuerying)
	{
		super(Release.class, corrector, beforeQuerying, afterQuerying);
		rule = FxUtil.constantStringBinding(formatRule(corrector));
	}

	@Override
	public StringBinding ruleTypeBinding()
	{
		return ruleType;
	}

	public static String getRuleType()
	{
		return ruleType.get();
	}

	@Override
	public StringBinding ruleBinding()
	{
		return rule;
	}

	private static String formatRule(ReleaseTagsCorrector corrector)
	{
		TagsReplacer replacer = corrector.getReplacer();
		StringBuilder sb = new StringBuilder();
		sb.append("If tags ");
		switch (replacer.getSearchMode())
		{
			case CONTAIN:
				sb.append("contain ");
				break;
			case EQUAL:
				sb.append("equal ");
				break;
		}
		sb.append('[');
		Joiner.on(", ").appendTo(sb, replacer.getSearchTags());
		sb.append(']');
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
		sb.append('[');
		Joiner.on(", ").appendTo(sb, replacer.getReplacement());
		sb.append(']');
		return sb.toString();
	}
}
package de.subcentral.watcher.settings;

import de.subcentral.core.correct.ReleaseTagsCorrector;
import de.subcentral.core.correct.TagsReplacer;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.util.StringUtil;
import de.subcentral.fx.FxUtil;
import javafx.beans.binding.StringBinding;

public class ReleaseTagsCorrectionRuleSettingsItem extends CorrectionRuleSettingsItem<Release, ReleaseTagsCorrector>
{
	private static final StringBinding	ruleType	= FxUtil.constantStringBinding("Release tags");
	private final StringBinding			rule;

	public ReleaseTagsCorrectionRuleSettingsItem(ReleaseTagsCorrector corrector, boolean beforeQuerying, boolean afterQuerying)
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
		TagsReplacer replacer = (TagsReplacer) corrector.getReplacer();
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
			default:
				sb.append(replacer.getSearchMode());
		}
		sb.append('[');
		StringUtil.COMMA_JOINER.appendTo(sb, replacer.getSearchTags());
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
			default:
				sb.append(replacer.getReplaceMode());
		}
		sb.append('[');
		StringUtil.COMMA_JOINER.appendTo(sb, replacer.getReplacement());
		sb.append(']');
		return sb.toString();
	}
}
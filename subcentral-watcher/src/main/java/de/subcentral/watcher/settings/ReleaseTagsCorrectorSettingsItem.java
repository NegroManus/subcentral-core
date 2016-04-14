package de.subcentral.watcher.settings;

import de.subcentral.core.correct.ReleaseTagsCorrector;
import de.subcentral.core.correct.TagsReplacer;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.util.StringUtil;
import de.subcentral.fx.FxBindings;
import javafx.beans.value.ObservableValue;

public class ReleaseTagsCorrectorSettingsItem extends CorrectorSettingsItem<Release, ReleaseTagsCorrector>
{
	private static final ObservableValue<String>	ruleType	= FxBindings.immutableObservableValue("Release tags");
	private final ObservableValue<String>			rule;

	public ReleaseTagsCorrectorSettingsItem(ReleaseTagsCorrector corrector, boolean beforeQuerying, boolean afterQuerying)
	{
		super(Release.class, corrector, beforeQuerying, afterQuerying);
		rule = FxBindings.immutableObservableValue(formatRule(corrector));
	}

	@Override
	public ObservableValue<String> ruleType()
	{
		return ruleType;
	}

	public static String getRuleType()
	{
		return ruleType.getValue();
	}

	@Override
	public ObservableValue<String> rule()
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
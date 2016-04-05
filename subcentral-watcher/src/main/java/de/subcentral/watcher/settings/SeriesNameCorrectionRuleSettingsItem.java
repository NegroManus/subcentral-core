package de.subcentral.watcher.settings;

import java.util.List;

import de.subcentral.core.correct.SeriesNameCorrector;
import de.subcentral.core.metadata.media.Series;
import de.subcentral.core.util.StringUtil;
import de.subcentral.fx.FxUtil;
import de.subcentral.fx.UserPattern;
import javafx.beans.binding.StringBinding;

public class SeriesNameCorrectionRuleSettingsItem extends CorrectionRuleSettingsItem<Series, SeriesNameCorrector>
{
	private static final StringBinding	ruleType	= FxUtil.constantStringBinding("Series name");
	private final StringBinding			rule;
	private final UserPattern			nameUserPattern;

	public SeriesNameCorrectionRuleSettingsItem(UserPattern nameUiPattern, String nameReplacement, List<String> aliasNamesReplacement, boolean beforeQuerying, boolean afterQuerying)
	{
		super(Series.class, buildCorrector(nameUiPattern, nameReplacement, aliasNamesReplacement), beforeQuerying, afterQuerying);
		rule = FxUtil.constantStringBinding(formatRule(value, nameUiPattern));
		this.nameUserPattern = nameUiPattern;
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

	public UserPattern getNameUserPattern()
	{
		return nameUserPattern;
	}

	private static SeriesNameCorrector buildCorrector(UserPattern namePattern, String nameReplacement, List<String> aliasNamesReplacement)
	{
		return new SeriesNameCorrector(namePattern.toPattern(), nameReplacement, aliasNamesReplacement, null);
	}

	private static String formatRule(SeriesNameCorrector corrector, UserPattern nameUserPattern)
	{
		StringBuilder sb = new StringBuilder();
		sb.append('"');
		sb.append(nameUserPattern.getPattern());
		sb.append("\" (");
		sb.append(nameUserPattern.getMode().name().charAt(0));
		sb.append(") -> ");
		sb.append(StringUtil.quoteString(corrector.getNameReplacement()));
		if (!corrector.getAliasNamesReplacement().isEmpty())
		{
			sb.append(" (aka ");
			StringUtil.COMMA_JOINER.appendTo(sb, corrector.getAliasNamesReplacement().stream().map((String alias) -> StringUtil.quoteString(alias)).iterator());
			sb.append(')');
		}
		return sb.toString();
	}
}
package de.subcentral.watcher.settings;

import java.util.List;

import de.subcentral.core.correct.SeriesNameCorrector;
import de.subcentral.core.metadata.media.Series;
import de.subcentral.core.util.StringUtil;
import de.subcentral.fx.FxBindings;
import de.subcentral.fx.UserPattern;
import javafx.beans.value.ObservableValue;

public class SeriesNameCorrectorSettingsItem extends CorrectorSettingsItem<Series, SeriesNameCorrector>
{
	private static final ObservableValue<String>	ruleType	= FxBindings.immutableObservableValue("Series name");
	private final ObservableValue<String>			rule;
	private final UserPattern						nameUserPattern;

	public SeriesNameCorrectorSettingsItem(UserPattern nameUiPattern, String nameReplacement, List<String> aliasNamesReplacement, boolean beforeQuerying, boolean afterQuerying)
	{
		super(Series.class, buildCorrector(nameUiPattern, nameReplacement, aliasNamesReplacement), beforeQuerying, afterQuerying);
		rule = FxBindings.immutableObservableValue(formatRule(item, nameUiPattern));
		this.nameUserPattern = nameUiPattern;
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
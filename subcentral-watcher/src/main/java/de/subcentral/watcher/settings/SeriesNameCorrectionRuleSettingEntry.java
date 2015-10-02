package de.subcentral.watcher.settings;

import de.subcentral.core.correction.SeriesNameCorrector;
import de.subcentral.core.metadata.media.Series;
import de.subcentral.core.util.StringUtil;
import de.subcentral.fx.FxUtil;
import de.subcentral.fx.UserPattern;
import javafx.beans.binding.StringBinding;

public class SeriesNameCorrectionRuleSettingEntry extends CorrectionRuleSettingEntry<Series, SeriesNameCorrector>
{
	private static final StringBinding	ruleType	= FxUtil.constantStringBinding("Series name");
	private final StringBinding			rule;
	private final UserPattern			nameUserPattern;

	public SeriesNameCorrectionRuleSettingEntry(UserPattern nameUiPattern, String nameReplacement, boolean beforeQuerying, boolean afterQuerying)
	{
		super(Series.class, buildCorrector(nameUiPattern, nameReplacement), beforeQuerying, afterQuerying);
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

	private static SeriesNameCorrector buildCorrector(UserPattern namePattern, String nameReplacement)
	{
		return new SeriesNameCorrector(namePattern.toPattern(), nameReplacement);
	}

	private static String formatRule(SeriesNameCorrector corrector, UserPattern nameUserPattern)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Replace \"");
		sb.append(nameUserPattern.getPattern());
		sb.append("\" (");
		sb.append(nameUserPattern.getMode().name());
		sb.append(") with ");
		sb.append(StringUtil.quoteString(corrector.getNameReplacement()));
		return sb.toString();
	}
}
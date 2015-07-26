package de.subcentral.watcher.settings;

import de.subcentral.core.metadata.media.Series;
import de.subcentral.core.standardizing.SeriesNameStandardizer;
import de.subcentral.core.util.StringUtil;
import de.subcentral.fx.FxUtil;
import de.subcentral.fx.UserPattern;
import javafx.beans.binding.StringBinding;

public class SeriesNameStandardizerSettingEntry extends StandardizerSettingEntry<Series, SeriesNameStandardizer>
{
	private static final StringBinding	standardizerType	= FxUtil.constantStringBinding("Series name");
	private final StringBinding			rule;
	private final UserPattern			nameUserPattern;

	public SeriesNameStandardizerSettingEntry(UserPattern nameUiPattern, String nameReplacement, boolean beforeQuerying, boolean afterQuerying)
	{
		super(Series.class, buildStandardizer(nameUiPattern, nameReplacement), beforeQuerying, afterQuerying);
		rule = FxUtil.constantStringBinding(formatRule(value, nameUiPattern));
		this.nameUserPattern = nameUiPattern;
	}

	private static SeriesNameStandardizer buildStandardizer(UserPattern namePattern, String nameReplacement)
	{
		return new SeriesNameStandardizer(namePattern.toPattern(), nameReplacement, null);
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

	public UserPattern getNameUserPattern()
	{
		return nameUserPattern;
	}

	public static String getStandardizerTypeString()
	{
		return standardizerType.get();
	}

	private static String formatRule(SeriesNameStandardizer standardizer, UserPattern nameUserPattern)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Replace \"");
		sb.append(nameUserPattern.getPattern());
		sb.append("\" (");
		sb.append(nameUserPattern.getMode().name());
		sb.append("), with ");
		sb.append(StringUtil.quoteString(standardizer.getNameReplacement()));
		return sb.toString();
	}
}
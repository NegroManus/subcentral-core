package de.subcentral.watcher.settings;

import javafx.beans.binding.StringBinding;
import de.subcentral.core.metadata.media.Series;
import de.subcentral.core.standardizing.SeriesNameStandardizer;
import de.subcentral.core.util.StringUtil;
import de.subcentral.fx.FxUtil;
import de.subcentral.fx.UserPattern;

public class SeriesNameStandardizerSettingEntry extends StandardizerSettingEntry<Series, SeriesNameStandardizer>
{
	private static final StringBinding	standardizerTypeAsString	= FxUtil.createConstantStringBinding("Series name");
	private final StringBinding			ruleAsString;
	private final UserPattern				nameUiPattern;

	public SeriesNameStandardizerSettingEntry(UserPattern nameUiPattern, String nameReplacement, boolean enabled)
	{
		super(Series.class, buildStandardizer(nameUiPattern, nameReplacement), enabled);
		ruleAsString = FxUtil.createConstantStringBinding(operationToString(value, nameUiPattern));
		this.nameUiPattern = nameUiPattern;
	}

	private static SeriesNameStandardizer buildStandardizer(UserPattern namePattern, String nameReplacement)
	{
		return new SeriesNameStandardizer(namePattern.toPattern(), nameReplacement, null);
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

	public UserPattern getNameUiPattern()
	{
		return nameUiPattern;
	}

	public static String getStandardizerTypeString()
	{
		return standardizerTypeAsString.get();
	}

	private static String operationToString(SeriesNameStandardizer standardizer, UserPattern nameUiPattern)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Replace \"");
		sb.append(nameUiPattern.getPattern());
		sb.append("\" (");
		sb.append(nameUiPattern.getMode().name());
		sb.append("), with ");
		sb.append(StringUtil.quoteString(standardizer.getNameReplacement()));
		return sb.toString();
	}
}
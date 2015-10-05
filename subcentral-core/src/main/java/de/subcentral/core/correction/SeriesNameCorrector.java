package de.subcentral.core.correction;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.media.Series;

/**
 * @implSpec #immutable #thread-safe
 */
public class SeriesNameCorrector implements Corrector<Series>
{
	private final Pattern				namePattern;
	private final String				nameReplacement;
	private final ImmutableList<String>	aliasNamesReplacement;
	private final String				titleReplacement;

	public SeriesNameCorrector(Pattern namePattern, String nameReplacement)
	{
		this(namePattern, nameReplacement, ImmutableList.of(), null);
	}

	public SeriesNameCorrector(Pattern namePattern, String nameReplacement, Iterable<String> aliasNamesReplacement)
	{
		this(namePattern, nameReplacement, aliasNamesReplacement, null);
	}

	public SeriesNameCorrector(Pattern namePattern, String nameReplacement, Iterable<String> aliasNamesReplacement, String titleReplacement)
	{
		this.namePattern = Objects.requireNonNull(namePattern, "namePattern");
		this.nameReplacement = nameReplacement;
		this.aliasNamesReplacement = ImmutableList.copyOf(aliasNamesReplacement);
		this.titleReplacement = titleReplacement;
	}

	public Pattern getNamePattern()
	{
		return namePattern;
	}

	public String getNameReplacement()
	{
		return nameReplacement;
	}

	public ImmutableList<String> getAliasNamesReplacement()
	{
		return aliasNamesReplacement;
	}

	public String getTitleReplacement()
	{
		return titleReplacement;
	}

	@Override
	public void correct(Series series, List<Correction> corrections)
	{
		if (series == null || series.getName() == null)
		{
			return;
		}
		String oldName = series.getName();
		if (namePattern.matcher(oldName).matches())
		{
			if (!oldName.equals(nameReplacement))
			{
				series.setName(nameReplacement);
				corrections.add(new Correction(series, Series.PROP_NAME.getPropName(), oldName, nameReplacement));
			}

			ImmutableList<String> oldAliasNames = ImmutableList.copyOf(series.getAliasNames());
			if (!oldAliasNames.equals(aliasNamesReplacement))
			{
				series.setAliasNames(aliasNamesReplacement);
				corrections.add(new Correction(series, Series.PROP_ALIAS_NAMES.getPropName(), oldAliasNames, aliasNamesReplacement));
			}

			String oldTitle = series.getTitle();
			if (!Objects.equals(oldTitle, titleReplacement))
			{
				series.setTitle(titleReplacement);
				corrections.add(new Correction(series, Series.PROP_TITLE.getPropName(), oldTitle, titleReplacement));
			}
		}
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(SeriesNameCorrector.class).add("namePattern", namePattern).add("nameReplacement", nameReplacement).add("titleReplacement", titleReplacement).toString();
	}
}

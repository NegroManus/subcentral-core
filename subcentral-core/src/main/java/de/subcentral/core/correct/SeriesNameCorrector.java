package de.subcentral.core.correct;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.media.Series;
import de.subcentral.core.util.ObjectUtil;

/**
 * @implSpec #immutable #thread-safe
 */
public class SeriesNameCorrector implements Corrector<Series>
{
	private final Pattern		namePattern;
	private final String		nameReplacement;
	private final List<String>	aliasNamesReplacement;
	private final String		titleReplacement;

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

	public List<String> getAliasNamesReplacement()
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
		if (namePattern.matcher(series.getName()).matches())
		{
			if (!series.getName().equals(nameReplacement))
			{
				String oldName = series.getName();
				series.setName(nameReplacement);
				corrections.add(new Correction(series, Series.PROP_NAME.getPropName(), oldName, nameReplacement, this));
			}
			if (!series.getAliasNames().equals(aliasNamesReplacement))
			{
				List<String> oldAliasNames = ImmutableList.copyOf(series.getAliasNames());
				series.setAliasNames(aliasNamesReplacement);
				corrections.add(new Correction(series, Series.PROP_ALIAS_NAMES.getPropName(), oldAliasNames, aliasNamesReplacement, this));
			}
			if (!Objects.equals(series.getTitle(), titleReplacement))
			{
				String oldTitle = series.getTitle();
				series.setTitle(titleReplacement);
				corrections.add(new Correction(series, Series.PROP_TITLE.getPropName(), oldTitle, titleReplacement, this));
			}
		}
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj instanceof SeriesNameCorrector)
		{
			SeriesNameCorrector o = (SeriesNameCorrector) obj;
			return ObjectUtil.patternsEqual(namePattern, o.namePattern) && Objects.equals(nameReplacement, o.nameReplacement) && aliasNamesReplacement.equals(o.aliasNamesReplacement)
					&& Objects.equals(titleReplacement, o.titleReplacement);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(SeriesNameCorrector.class, ObjectUtil.patternHashCode(namePattern), nameReplacement, aliasNamesReplacement, titleReplacement);
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(SeriesNameCorrector.class)
				.add("namePattern", namePattern)
				.add("nameReplacement", nameReplacement)
				.add("aliasNamesReplacement", aliasNamesReplacement)
				.add("titleReplacement", titleReplacement)
				.toString();
	}
}

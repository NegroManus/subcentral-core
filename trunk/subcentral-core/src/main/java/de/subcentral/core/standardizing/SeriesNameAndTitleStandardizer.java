package de.subcentral.core.standardizing;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.model.media.Series;

/**
 * @implSpec #immutable #thread-safe
 */
public class SeriesNameAndTitleStandardizer implements Standardizer<Series>
{
	private final Pattern	namePattern;
	private final String	nameReplacement;
	private final String	titleReplacement;

	public SeriesNameAndTitleStandardizer(Pattern namePattern, String nameReplacement, String titleReplacement)
	{
		this.namePattern = Objects.requireNonNull(namePattern, "namePattern");
		this.nameReplacement = nameReplacement;
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

	public String getTitleReplacement()
	{
		return titleReplacement;
	}

	@Override
	public List<StandardizingChange> standardize(Series series)
	{
		if (series == null)
		{
			return ImmutableList.of();
		}
		String oldName = series.getName();
		if (oldName != null && namePattern.matcher(oldName).matches())
		{
			ImmutableList.Builder<StandardizingChange> changes = ImmutableList.builder();
			if (!oldName.equals(nameReplacement))
			{
				series.setName(nameReplacement);
				changes.add(new StandardizingChange(series, Series.PROP_NAME.getPropName(), oldName, nameReplacement));
			}

			String oldTitle = series.getTitle();
			if (!Objects.equals(oldTitle, titleReplacement))
			{
				series.setTitle(titleReplacement);
				changes.add(new StandardizingChange(series, Series.PROP_TITLE.getPropName(), oldTitle, titleReplacement));
			}
			return changes.build();
		}
		return ImmutableList.of();
	}
}

package de.subcentral.core.standardizing;

import java.util.List;
import java.util.Objects;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.model.media.Series;
import de.subcentral.core.util.PatternReplacer;

public class SeriesNameStandardizer implements Standardizer<Series>
{
	private final PatternReplacer	patternReplacer;

	public SeriesNameStandardizer(PatternReplacer patternReplacer)
	{
		this.patternReplacer = Objects.requireNonNull(patternReplacer, "patternReplacer");
	}

	public PatternReplacer getPatternReplacer()
	{
		return patternReplacer;
	}

	@Override
	public List<StandardizingChange> standardize(Series series)
	{
		String name = series.getName();
		if (name == null)
		{
			return ImmutableList.of();
		}
		String stdzdName = patternReplacer.apply(name);
		if (!stdzdName.equals(name))
		{
			series.setName(stdzdName);
			return ImmutableList.of(new StandardizingChange(series, Series.PROP_NAME.getPropName(), name, stdzdName));
		}
		return ImmutableList.of();
	}
}

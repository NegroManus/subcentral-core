package de.subcentral.core.naming;

import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Joiner;

import de.subcentral.core.media.Episode;
import de.subcentral.core.util.StringUtil;

public class DatedEpisodeNamer extends AbstractEpisodeNamer
{
	private TemporalQuery<String>	episodeDateQuery	= new DefaultDateQuery();
	private String					episodeDateFormat	= " %s";

	public TemporalQuery<String> getEpisodeDateQuery()
	{
		return episodeDateQuery;
	}

	public void setEpisodeDateQuery(TemporalQuery<String> episodeDateQuery)
	{
		this.episodeDateQuery = episodeDateQuery;
	}

	public String getEpisodeDateFormat()
	{
		return episodeDateFormat;
	}

	public void setEpisodeDateFormat(String episodeDateFormat)
	{
		this.episodeDateFormat = episodeDateFormat;
	}

	@Override
	public String name(Episode epi, boolean includeSeries, boolean includeSeason, NamingService namingService)
	{
		if (epi == null)
		{
			return null;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(StringUtil.replace(epi.getSeries().getNameOrCompute(), seriesNameReplacer));
		if (epi.getDate() != null)
		{
			String printedDate = episodeDateQuery.queryFrom(epi.getDate());
			if (!StringUtils.isEmpty(printedDate))
			{
				sb.append(String.format(episodeDateFormat, printedDate));
			}
		}
		if (alwaysIncludeEpisodeTitle && epi.isTitled())
		{
			sb.append(String.format(episodeTitleFormat, StringUtil.replace(epi.getTitle(), episodeTitleReplacer)));
		}
		return sb.toString();
	}

	public class DefaultDateQuery implements TemporalQuery<String>
	{
		@Override
		public String queryFrom(TemporalAccessor date)
		{
			if (date == null)
			{
				return null;
			}
			List<String> fragments = new ArrayList<>(3);
			if (date.isSupported(ChronoField.YEAR))
			{
				long year = date.getLong(ChronoField.YEAR);
				fragments.add(String.format("%04d", year));
			}
			if (date.isSupported(ChronoField.MONTH_OF_YEAR))
			{
				long month = date.getLong(ChronoField.MONTH_OF_YEAR);
				fragments.add(String.format("%02d", month));
			}
			if (date.isSupported(ChronoField.DAY_OF_MONTH))
			{
				long day = date.getLong(ChronoField.DAY_OF_MONTH);
				fragments.add(String.format("%02d", day));
			}
			return Joiner.on(' ').skipNulls().join(fragments);
		}
	}
}

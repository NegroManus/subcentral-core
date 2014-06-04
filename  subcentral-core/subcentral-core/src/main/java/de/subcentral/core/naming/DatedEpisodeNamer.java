package de.subcentral.core.naming;

import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Joiner;

import de.subcentral.core.media.Episode;
import de.subcentral.core.util.Replacer;
import de.subcentral.core.util.StringUtil;

public class DatedEpisodeNamer implements Namer<Episode>
{
	private Replacer				seriesNameReplacer			= NamingStandards.STANDARD_REPLACER;
	private String					episodeDatePrefix			= ".";
	private TemporalQuery<String>	episodeDateQuery			= new DefaultDateQuery();
	private boolean					alwaysIncludeEpisodeTitle	= false;
	private String					episodeTitlePrefix			= ".";
	private Replacer				episodeTitleReplacer		= NamingStandards.STANDARD_REPLACER;

	@Override
	public Class<Episode> getType()
	{
		return Episode.class;
	}

	@Override
	public String name(Episode epi, NamingService namingService)
	{
		if (epi == null)
		{
			return null;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(StringUtil.replace(epi.getSeries().getName(), seriesNameReplacer));
		if (epi.getDate() != null)
		{
			String printedDate = episodeDateQuery.queryFrom(epi.getDate());
			if (!StringUtils.isEmpty(printedDate))
			{
				sb.append(episodeDatePrefix);
				sb.append(printedDate);
			}
		}
		if (alwaysIncludeEpisodeTitle && epi.isTitled())
		{
			sb.append(episodeTitlePrefix);
			sb.append(StringUtil.replace(epi.getTitle(), episodeTitleReplacer));
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
			return Joiner.on('.').skipNulls().join(fragments);
		}
	}
}

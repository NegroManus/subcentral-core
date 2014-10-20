package de.subcentral.core.parsing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

import de.subcentral.core.model.media.Episode;
import de.subcentral.core.util.SimplePropDescriptor;

public class MultiEpisodeMapper extends AbstractMapper<List<Episode>>
{
	private final Mapper<Episode>	episodeMapper;

	public MultiEpisodeMapper(Mapper<Episode> episodeMapper)
	{
		this.episodeMapper = Objects.requireNonNull(episodeMapper, "episodeMapper");
	}

	public Mapper<Episode> getEpisodeMapper()
	{
		return episodeMapper;
	}

	@Override
	protected List<Episode> doMap(Map<SimplePropDescriptor, String> props, PropParsingService propParsingService)
	{
		List<Episode> media = parseMultiEpisode(props, propParsingService, Episode.PROP_NUMBER_IN_SERIES);
		if (!media.isEmpty())
		{
			return media;
		}
		media = parseMultiEpisode(props, propParsingService, Episode.PROP_NUMBER_IN_SEASON);
		if (!media.isEmpty())
		{
			return media;
		}
		return ImmutableList.of();
	}

	private List<Episode> parseMultiEpisode(Map<SimplePropDescriptor, String> props, PropParsingService propParsingService,
			SimplePropDescriptor epiNumProp)
	{
		String numString = props.get(epiNumProp);
		if (StringUtils.isBlank(numString))
		{
			return ImmutableList.of();
		}
		String[] epiNums = extractEpiNums(numString);
		switch (epiNums.length)
		{
			case 0:
				return ImmutableList.of();
			case 1:
				return ImmutableList.of(episodeMapper.map(props, propParsingService));
			default:
				List<Episode> episodes = new ArrayList<>(epiNums.length);
				for (String epiNum : epiNums)
				{
					Map<SimplePropDescriptor, String> propsForEpi = new HashMap<>(props);
					// overwrite episode num
					propsForEpi.put(epiNumProp, epiNum);
					episodes.add(episodeMapper.map(propsForEpi, propParsingService));
				}
				return episodes;
		}
	}

	private static final Pattern	RANGE_PATTERN		= Pattern.compile("E(\\d{1,2})-E(\\d{1,2})", Pattern.CASE_INSENSITIVE);
	// "(?:X) 	X, as a non-capturing group"
	private static final Pattern	ADDITION_PATTERN	= Pattern.compile("E(\\d{1,2})((?:\\+?E\\d{1,2})+)", Pattern.CASE_INSENSITIVE);

	public static boolean containsMultiEpisode(Map<SimplePropDescriptor, String> props)
	{
		return extractEpiNums(props.get(Episode.PROP_NUMBER_IN_SERIES)).length >= 2
				|| extractEpiNums(props.get(Episode.PROP_NUMBER_IN_SEASON)).length >= 2;
	}

	private static String[] extractEpiNums(String numString)
	{
		if (StringUtils.isBlank(numString))
		{
			return new String[] { numString };
		}
		Matcher matcher = RANGE_PATTERN.matcher(numString);
		if (matcher.find())
		{
			int start = Integer.parseInt(matcher.group(1));
			int end = Integer.parseInt(matcher.group(2));
			if (end < start)
			{
				// switch
				int helper = start;
				start = end;
				end = helper;
			}
			String[] nums = new String[end - start + 1];
			for (int i = 0, epiNum = start; epiNum < end + 1; i++, epiNum++)
			{
				nums[i] = Integer.toString(epiNum);
			}
			return nums;
		}
		matcher = ADDITION_PATTERN.matcher(numString);
		if (matcher.find())
		{
			List<String> epiNums = Splitter.on(CharMatcher.noneOf("0123456789")).omitEmptyStrings().trimResults().splitToList(matcher.group(2));
			String[] nums = new String[1 + epiNums.size()];
			nums[0] = matcher.group(1);
			for (int i = 0; i < epiNums.size(); i++)
			{
				nums[i + 1] = epiNums.get(i);
			}
			return nums;
		}
		return new String[] { numString };
	}

}

package de.subcentral.core.parse;

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

import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.util.SimplePropDescriptor;

public class MultiEpisodeMapper implements Mapper<List<Episode>>
{
	private final Mapper<Episode> episodeMapper;

	public MultiEpisodeMapper(Mapper<Episode> episodeMapper)
	{
		this.episodeMapper = Objects.requireNonNull(episodeMapper, "episodeMapper");
	}

	public Mapper<Episode> getEpisodeMapper()
	{
		return episodeMapper;
	}

	@Override
	public List<Episode> map(Map<SimplePropDescriptor, String> props)
	{
		try
		{
			List<Episode> media = parseMultiEpisode(props, Episode.PROP_NUMBER_IN_SERIES);
			if (!media.isEmpty())
			{
				return media;
			}
			media = parseMultiEpisode(props, Episode.PROP_NUMBER_IN_SEASON);
			if (!media.isEmpty())
			{
				return media;
			}
			return ImmutableList.of();
		}
		catch (RuntimeException e)
		{
			throw new MappingException(props, null, "Exception while mapping to a multi episode", e);
		}
	}

	private List<Episode> parseMultiEpisode(Map<SimplePropDescriptor, String> props, SimplePropDescriptor epiNumProp)
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
				return ImmutableList.of(episodeMapper.map(props));
			default:
				List<Episode> episodes = new ArrayList<>(epiNums.length);
				for (String epiNum : epiNums)
				{
					Map<SimplePropDescriptor, String> propsForEpi = new HashMap<>(props);
					// overwrite episode num
					propsForEpi.put(epiNumProp, epiNum);
					episodes.add(episodeMapper.map(propsForEpi));
				}
				return episodes;
		}
	}

	/**
	 * Matches "E01-E02", "E01-02", "01-02", ... (two number blocks, separated by a hyphen).
	 */
	private static final Pattern	RANGE_PATTERN		= Pattern.compile("(\\d{1,2})-\\D*(\\d{1,2})", Pattern.CASE_INSENSITIVE);
	/**
	 * Matches "E01E02", "E01+E02", "E01+02", "E01E02E03", "E01+E02+E03", "E01+02+03" ... (several number blocks, separated by non digit chars).
	 * 
	 * "(?:X) 	X, as a non-capturing group"
	 */
	private static final Pattern	ADDITION_PATTERN	= Pattern.compile("(\\d{1,2})((?:\\D+\\d{1,2})+)", Pattern.CASE_INSENSITIVE);

	public static boolean containsMultiEpisode(Map<SimplePropDescriptor, String> props)
	{
		// first check for multiple numbers in season because most episode are part of a season
		return extractEpiNums(props.get(Episode.PROP_NUMBER_IN_SEASON)).length >= 2 || extractEpiNums(props.get(Episode.PROP_NUMBER_IN_SERIES)).length >= 2;
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
				// swap start and end
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
			List<String> additionalEpiNums = Splitter.on(CharMatcher.JAVA_DIGIT.negate()).omitEmptyStrings().trimResults().splitToList(matcher.group(2));
			String[] nums = new String[1 + additionalEpiNums.size()];
			nums[0] = matcher.group(1);
			for (int i = 0; i < additionalEpiNums.size(); i++)
			{
				nums[i + 1] = additionalEpiNums.get(i);
			}
			return nums;
		}
		return new String[] { numString };
	}

}

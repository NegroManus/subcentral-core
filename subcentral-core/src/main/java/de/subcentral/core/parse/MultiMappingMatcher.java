package de.subcentral.core.parse;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class MultiMappingMatcher<K> implements MappingMatcher<K>
{
	private final List<MappingMatcher<K>> matchers;

	public MultiMappingMatcher(Iterable<MappingMatcher<K>> matchers)
	{
		this.matchers = ImmutableList.copyOf(matchers);
	}

	@SafeVarargs
	public MultiMappingMatcher(MappingMatcher<K>... matchers)
	{
		this.matchers = ImmutableList.copyOf(matchers);
	}

	public List<MappingMatcher<K>> getMatchers()
	{
		return matchers;
	}

	@Override
	public Map<K, String> match(String text)
	{
		for (MappingMatcher<K> matcher : matchers)
		{
			Map<K, String> matchResult = matcher.match(text);
			if (!matchResult.isEmpty())
			{
				return matchResult;
			}
		}
		return ImmutableMap.of();
	}
}

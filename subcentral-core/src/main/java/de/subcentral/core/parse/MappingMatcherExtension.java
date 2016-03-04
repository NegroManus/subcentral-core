package de.subcentral.core.parse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.subcentral.core.util.SimplePropDescriptor;

public class MappingMatcherExtension
{
	private String								patternPrefix;
	private String								patternSuffix;
	private List<SimplePropDescriptor>			prefixProps			= new ArrayList<>();
	private List<SimplePropDescriptor>			suffixProps			= new ArrayList<>();
	private Map<SimplePropDescriptor, String>	predefinedMatches	= new HashMap<>();

	public String getPatternPrefix()
	{
		return patternPrefix;
	}

	public void setPatternPrefix(String patternPrefix)
	{
		this.patternPrefix = patternPrefix;
	}

	public String getPatternSuffix()
	{
		return patternSuffix;
	}

	public void setPatternSuffix(String patternSuffix)
	{
		this.patternSuffix = patternSuffix;
	}

	public List<SimplePropDescriptor> getPrefixProps()
	{
		return prefixProps;
	}

	public void setPrefixProps(List<SimplePropDescriptor> prefixProps)
	{
		this.prefixProps = prefixProps;
	}

	public List<SimplePropDescriptor> getSuffixProps()
	{
		return suffixProps;
	}

	public void setSuffixProps(List<SimplePropDescriptor> suffixProps)
	{
		this.suffixProps = suffixProps;
	}

	public Map<SimplePropDescriptor, String> getPredefinedMatches()
	{
		return predefinedMatches;
	}

	public void setPredefinedMatches(Map<SimplePropDescriptor, String> predefinedMatches)
	{
		this.predefinedMatches = predefinedMatches;
	}

	public List<PatternMappingMatcher<SimplePropDescriptor>> extend(List<PatternMappingMatcher<SimplePropDescriptor>> origMatchers)
	{
		ImmutableList.Builder<PatternMappingMatcher<SimplePropDescriptor>> extendedMatchers = ImmutableList.builder();
		for (PatternMappingMatcher<SimplePropDescriptor> origMatcher : origMatchers)
		{
			// build the extended pattern
			Pattern extendedPattern = Pattern.compile(patternPrefix + origMatcher.getPattern() + patternSuffix, origMatcher.getPattern().flags());
			ImmutableMap.Builder<Integer, SimplePropDescriptor> extendedGrps = ImmutableMap.builder();

			// add prefix groups
			int grpNumCounter = 0;
			for (SimplePropDescriptor prefixProp : prefixProps)
			{
				extendedGrps.put(grpNumCounter++, prefixProp);
			}

			// transform the groups (use TreeMap to have them sorted by group number)
			for (Map.Entry<Integer, SimplePropDescriptor> origGrp : new TreeMap<>(origMatcher.getGroups()).entrySet())
			{
				extendedGrps.put(grpNumCounter++, origGrp.getValue());
			}

			// add suffix groups
			for (SimplePropDescriptor suffixProp : suffixProps)
			{
				extendedGrps.put(grpNumCounter++, suffixProp);
			}

			Map<SimplePropDescriptor, String> extendedPredefMatches = new HashMap<>();
			extendedPredefMatches.putAll(origMatcher.getPredefinedMatches());
			extendedPredefMatches.putAll(predefinedMatches);

			PatternMappingMatcher<SimplePropDescriptor> matcher = new PatternMappingMatcher<>(extendedPattern, extendedGrps.build(), extendedPredefMatches);
			extendedMatchers.add(matcher);
		}
		return extendedMatchers.build();
	}
}
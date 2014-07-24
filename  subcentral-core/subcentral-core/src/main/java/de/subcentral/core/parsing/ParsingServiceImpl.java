package de.subcentral.core.parsing;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jsoup.helper.Validate;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.subcentral.core.util.SimplePropDescriptor;

public class ParsingServiceImpl implements ParsingService
{
	private String								domain;
	private Multimap<Class<?>, MappingMatcher>	matchers	= HashMultimap.create();
	private MappingService						mappingService;

	@Override
	public String getDomain()
	{
		return domain;
	}

	public void setDomain(String domain)
	{
		this.domain = domain;
	}

	public Multimap<Class<?>, MappingMatcher> getMatchers()
	{
		return matchers;
	}

	public void setMatchers(Multimap<Class<?>, MappingMatcher> matchers)
	{
		this.matchers = matchers;
	}

	public boolean registerMatcher(Class<?> typeClass, MappingMatcher matcher)
	{
		return this.matchers.put(typeClass, matcher);
	}

	public boolean unregisterMatcher(MappingMatcher matcher)
	{
		boolean existed = false;
		for (Entry<Class<?>, MappingMatcher> entry : matchers.entries())
		{
			if (matcher.equals(entry.getValue()))
			{
				existed = matchers.remove(entry.getKey(), entry.getValue());
			}
		}
		return existed;
	}

	public MappingService getMappingService()
	{
		return mappingService;
	}

	public void setMappingService(MappingService mappingService)
	{
		this.mappingService = mappingService;
	}

	@Override
	public Object parse(String name, Map<SimplePropDescriptor, String> additionalInfo)
	{
		for (Entry<Class<?>, MappingMatcher> entry : matchers.entries())
		{
			Map<SimplePropDescriptor, String> matchResult = entry.getValue().match(name);
			if (matchResult != null)
			{
				return mappingService.map(combineInfo(matchResult, additionalInfo), entry.getKey());
			}
		}
		return null;
	}

	private Map<SimplePropDescriptor, String> combineInfo(Map<SimplePropDescriptor, String> matchResult,
			Map<SimplePropDescriptor, String> additionalInfo)
	{
		Map<SimplePropDescriptor, String> combinedInfo = new HashMap<>(matchResult.size() + additionalInfo.size());
		combinedInfo.putAll(matchResult);
		combinedInfo.putAll(additionalInfo);
		return combinedInfo;
	}

	@Override
	public <T> T parse(String name, Class<T> type, Map<SimplePropDescriptor, String> additionalInfo)
	{
		Validate.notNull(type, "type cannot be null");
		for (MappingMatcher matcher : matchers.get(type))
		{
			Map<SimplePropDescriptor, String> matchResult = matcher.match(name);
			if (matchResult != null)
			{
				return (T) mappingService.map(combineInfo(matchResult, additionalInfo), type);
			}
		}
		return null;
	}
}

package de.subcentral.core.parsing;

import java.util.HashMap;
import java.util.Map;

import de.subcentral.core.util.SimplePropertyDescriptor;

public class ParsingServiceImpl implements ParsingService
{
	private String							domain;
	private Map<MappingMatcher, Class<?>>	matchers	= new HashMap<>();
	private MappingService					mappingService;

	@Override
	public String getDomain()
	{
		return domain;
	}

	public void setDomain(String domain)
	{
		this.domain = domain;
	}

	public Map<MappingMatcher, Class<?>> getMatchers()
	{
		return matchers;
	}

	public void setMatchers(Map<MappingMatcher, Class<?>> matchers)
	{
		this.matchers = matchers;
	}

	public Class<?> registerMatcher(MappingMatcher matcher, Class<?> typeClass)
	{
		return this.matchers.put(matcher, typeClass);
	}

	public Class<?> unregisterMatcher(MappingMatcher matcher)
	{
		return matchers.remove(matcher);
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
	public Object parse(String name, Map<SimplePropertyDescriptor, String> additionalInfo)
	{
		for (Map.Entry<MappingMatcher, Class<?>> entry : matchers.entrySet())
		{
			Map<SimplePropertyDescriptor, String> matchResult = entry.getKey().map(name);
			if (matchResult != null)
			{
				return mappingService.map(combineInfo(matchResult, additionalInfo), entry.getValue());
			}
		}
		return null;
	}

	private Map<SimplePropertyDescriptor, String> combineInfo(Map<SimplePropertyDescriptor, String> matchResult,
			Map<SimplePropertyDescriptor, String> additionalInfo)
	{
		Map<SimplePropertyDescriptor, String> combinedInfo = new HashMap<>(matchResult.size() + additionalInfo.size());
		combinedInfo.putAll(matchResult);
		combinedInfo.putAll(additionalInfo);
		return combinedInfo;
	}
}

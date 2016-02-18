package de.subcentral.core.parse;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import com.google.common.base.MoreObjects;

/**
 * 
 * @implSpec #thread-safe
 *
 */
public class TypeBasedParsingService implements ParsingService
{
	private final String				domain;
	private final List<ParserEntry<?>>	parserEntries	= new CopyOnWriteArrayList<>();

	public TypeBasedParsingService(String domain)
	{
		this.domain = Objects.requireNonNull(domain, "domain");
	}

	@Override
	public String getDomain()
	{
		return domain;
	}

	@Override
	public Set<Class<?>> getSupportedTargetTypes()
	{
		return parserEntries.stream().map(ParserEntry::getTargetType).collect(Collectors.toSet());
	}

	public List<ParserEntry<?>> getParserEntries()
	{
		return parserEntries;
	}

	public List<Parser<?>> getParsers()
	{
		return parserEntries.stream().map(ParserEntry::getParser).collect(Collectors.toList());
	}

	public <T> void register(Class<T> targetType, Parser<T> parser)
	{
		parserEntries.add(new ParserEntry<T>(parser, targetType));
	}

	public <T> void registerAll(Class<T> targetType, Iterable<Parser<T>> parsers)
	{
		for (Parser<T> p : parsers)
		{
			register(targetType, p);
		}
	}

	public boolean unregister(Parser<?> parser)
	{
		Iterator<ParserEntry<?>> iter = parserEntries.iterator();
		while (iter.hasNext())
		{
			ParserEntry<?> entry = iter.next();
			if (entry.parser.equals(parser))
			{
				iter.remove();
				return true;
			}
		}
		return false;
	}

	public boolean unregisterAll(Iterable<Parser<?>> parsers)
	{
		boolean changed = false;
		for (Parser<?> p : parsers)
		{
			if (unregister(p))
			{
				changed = true;
			}
		}
		return changed;
	}

	public void unregisterAll()
	{
		parserEntries.clear();
	}

	@Override
	public Object parse(String text) throws ParsingException
	{
		for (ParserEntry<?> entry : parserEntries)
		{
			Object parsedObj = entry.parser.parse(text);
			if (parsedObj != null)
			{
				return parsedObj;
			}
		}
		return null;
	}

	@Override
	public <T> T parse(String text, Class<T> targetType) throws ParsingException
	{
		Objects.requireNonNull(targetType, "targetType cannot be null. For untyped parsing use #parse(String).");
		for (ParserEntry<?> entry : parserEntries)
		{
			if (targetType.isAssignableFrom(entry.targetType))
			{
				// save cast because targetType is super type of parser's type
				@SuppressWarnings("unchecked")
				Parser<? extends T> parser = (Parser<? extends T>) entry.parser;
				T parsedObj = parser.parse(text);
				if (parsedObj != null)
				{
					return parsedObj;
				}
			}
		}
		return null;
	}

	@Override
	public Object parse(String text, Set<Class<?>> targetTypes) throws ParsingException
	{
		if (targetTypes.isEmpty())
		{
			return parse(text);
		}
		for (ParserEntry<?> entry : parserEntries)
		{
			for (Class<?> targetType : targetTypes)
			{
				if (targetType.isAssignableFrom(entry.targetType))
				{
					Parser<?> parser = entry.parser;
					Object parsedObj = parser.parse(text);
					if (parsedObj != null)
					{
						return parsedObj;
					}
				}
			}
		}
		return null;
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(TypeBasedParsingService.class).add("domain", domain).toString();
	}

	public static final class ParserEntry<T>
	{
		private final Parser<T>	parser;
		private final Class<T>	targetType;

		private ParserEntry(Parser<T> parser, Class<T> targetType)
		{
			this.parser = Objects.requireNonNull(parser, "parser");
			this.targetType = Objects.requireNonNull(targetType, "targetType");
		}

		public Parser<T> getParser()
		{
			return parser;
		}

		public Class<T> getTargetType()
		{
			return targetType;
		}

		@Override
		public String toString()
		{
			return MoreObjects.toStringHelper(ParserEntry.class).add("parser", parser).add("targetType", targetType).toString();
		}
	}
}
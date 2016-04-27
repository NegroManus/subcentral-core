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
	private final String				name;
	private final List<ParserEntry<?>>	parsers	= new CopyOnWriteArrayList<>();

	public TypeBasedParsingService(String name)
	{
		this.name = Objects.requireNonNull(name, "name");
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public Set<Class<?>> getSupportedTargetTypes()
	{
		return parsers.stream().map(ParserEntry::getTargetType).collect(Collectors.toSet());
	}

	public List<ParserEntry<?>> getParserEntries()
	{
		return parsers;
	}

	public List<Parser<?>> getParsers()
	{
		return parsers.stream().map(ParserEntry::getParser).collect(Collectors.toList());
	}

	public <T> void register(Class<T> targetType, Parser<T> parser)
	{
		parsers.add(new ParserEntry<T>(targetType, parser));
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
		Iterator<ParserEntry<?>> iter = parsers.iterator();
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
		parsers.clear();
	}

	@Override
	public Object parse(String text)
	{
		for (ParserEntry<?> entry : parsers)
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
	public <T> T parse(String text, Class<T> targetType)
	{
		Objects.requireNonNull(targetType, "targetType cannot be null. For untyped parsing use #parse(String).");
		for (ParserEntry<?> entry : parsers)
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
	public Object parse(String text, Set<Class<?>> targetTypes)
	{
		if (targetTypes.isEmpty())
		{
			return parse(text);
		}
		for (ParserEntry<?> entry : parsers)
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
		return MoreObjects.toStringHelper(TypeBasedParsingService.class).add("name", name).toString();
	}

	public static final class ParserEntry<T>
	{
		private final Class<T>	targetType;
		private final Parser<T>	parser;

		private ParserEntry(Class<T> targetType, Parser<T> parser)
		{
			this.targetType = Objects.requireNonNull(targetType, "targetType");
			this.parser = Objects.requireNonNull(parser, "parser");
		}

		public Class<T> getTargetType()
		{
			return targetType;
		}

		public Parser<T> getParser()
		{
			return parser;
		}

		@Override
		public String toString()
		{
			return MoreObjects.toStringHelper(ParserEntry.class).add("targetType", targetType).add("parser", parser).toString();
		}
	}
}

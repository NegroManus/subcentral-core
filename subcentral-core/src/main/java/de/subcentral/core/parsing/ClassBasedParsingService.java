package de.subcentral.core.parsing;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * 
 * @implSpec #thread-safe
 *
 */
public class ClassBasedParsingService implements ParsingService
{
	private final String				domain;
	private final List<ParserEntry<?>>	parserEntries	= new CopyOnWriteArrayList<>();

	public ClassBasedParsingService(String domain)
	{
		this.domain = Objects.requireNonNull(domain, "domain");
	}

	@Override
	public String getDomain()
	{
		return domain;
	}

	@Override
	public Set<Class<?>> getTargetTypes()
	{
		return parserEntries.stream().map((entry) -> entry.targetType).collect(Collectors.toSet());
	}

	public List<ParserEntry<?>> getParserEntries()
	{
		return parserEntries;
	}

	public ImmutableList<Parser<?>> getParsers()
	{
		ImmutableList.Builder<Parser<?>> parsers = ImmutableList.builder();
		for (ParserEntry<?> entry : parserEntries)
		{
			parsers.add(entry.parser);
		}
		return parsers.build();
	}

	public <T> ImmutableList<Parser<? extends T>> getParsersForType(Class<T> targetType)
	{
		Objects.requireNonNull(targetType, "targetType");
		ImmutableList.Builder<Parser<? extends T>> parsers = ImmutableList.builder();
		for (ParserEntry<?> entry : parserEntries)
		{
			if (targetType.isAssignableFrom(entry.targetType))
			{
				// safe cast because targetType is superClass of parser's target type
				@SuppressWarnings("unchecked")
				Parser<? extends T> parser = (Parser<? extends T>) entry.parser;
				parsers.add(parser);
			}
		}
		return parsers.build();
	}

	public <T> void registerAllParsers(Class<T> targetType, Iterable<Parser<T>> parsers)
	{
		for (Parser<T> p : parsers)
		{
			registerParser(targetType, p);
		}
	}

	public boolean unregisterAllParsers(Iterable<Parser<?>> parsers)
	{
		boolean changed = false;
		for (Parser<?> p : parsers)
		{
			if (unregisterParser(p))
			{
				changed = true;
			}
		}
		return changed;
	}

	public <T> void registerParser(Class<T> targetType, Parser<T> parser)
	{
		parserEntries.add(new ParserEntry<T>(parser, targetType));
	}

	public boolean unregisterParser(Parser<?> parser)
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

	public void unregisterAllParsers()
	{
		parserEntries.clear();
	}

	@Override
	public Object parse(String text) throws NoMatchException, ParsingException
	{
		ParsingUtils.requireNotBlank(text);
		for (ParserEntry<?> entry : parserEntries)
		{
			try
			{
				return entry.parser.parse(text);
			}
			catch (NoMatchException e)
			{
				// this parser could no match
				// ignore and move on to the next
				continue;
			}
		}
		throw buildNoMatchException(text, ImmutableSet.of());
	}

	@Override
	public <T> T parse(String text, Class<T> targetType) throws NoMatchException, ParsingException
	{
		ParsingUtils.requireNotBlank(text);
		Objects.requireNonNull(targetType, "targetType cannot be null. For untyped parsing use " + getClass().getName() + ".parse(String).");
		for (ParserEntry<?> entry : parserEntries)
		{
			if (targetType.isAssignableFrom(entry.targetType))
			{
				try
				{
					// save cast because targetClass is superClass of parsers type
					@SuppressWarnings("unchecked")
					Parser<? extends T> parser = (Parser<? extends T>) entry.parser;
					return parser.parse(text);
				}
				catch (NoMatchException e)
				{
					// this parser could no match
					// ignore and move on to the next
					continue;
				}
			}
		}
		throw buildNoMatchException(text, ImmutableSet.of(targetType));
	}

	@Override
	public Object parse(String text, Set<Class<?>> targetTypes) throws NoMatchException, ParsingException
	{
		ParsingUtils.requireNotBlank(text);
		if (targetTypes == null || targetTypes.isEmpty())
		{
			return parse(text);
		}
		for (ParserEntry<?> entry : parserEntries)
		{
			for (Class<?> targetClass : targetTypes)
			{
				if (targetClass.isAssignableFrom(entry.targetType))
				{
					try
					{
						Parser<?> parser = entry.parser;
						return parser.parse(text);
					}
					catch (NoMatchException e)
					{
						// this parser could no match
						// ignore and move on to the next
						continue;
					}
				}
			}
		}
		throw buildNoMatchException(text, targetTypes);
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(ClassBasedParsingService.class).add("domain", domain).toString();
	}

	private static NoMatchException buildNoMatchException(String text, Set<Class<?>> targetTypes)
	{
		// build Exception message
		StringBuilder msg = new StringBuilder();
		msg.append("No parser ");
		if (targetTypes.isEmpty())
		{
			msg.append("that produces any target type of");
			msg.append(targetTypes);
			msg.append(' ');
		}
		msg.append("could parse the text");

		throw new NoMatchException(text, targetTypes, msg.toString());
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
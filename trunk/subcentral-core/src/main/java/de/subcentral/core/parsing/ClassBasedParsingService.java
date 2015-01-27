package de.subcentral.core.parsing;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import de.subcentral.core.util.ReflectionUtil;

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
		return parserEntries.stream().map((entry) -> entry.getTargetType()).collect(Collectors.toSet());
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

	public void registerAllParsers(Iterable<Parser<?>> parsers)
	{
		for (Parser<?> p : parsers)
		{
			registerParser(p);
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

	@SuppressWarnings("unchecked")
	public <T> void registerParser(Parser<T> parser)
	{
		parserEntries.add(new ParserEntry<T>(parser, (Class<T>) ReflectionUtil.getActualTypeArg(parser.getClass(), 0)));
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

	@Override
	public Object parse(String text) throws NoMatchException, ParsingException
	{
		ParsingUtils.requireNotBlank(text, null);
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
		throw buildNoMatchException(text, null);
	}

	@Override
	public <T> T parse(String text, Class<T> targetClass) throws NoMatchException, ParsingException
	{
		ParsingUtils.requireNotBlank(text, targetClass);
		Objects.requireNonNull(targetClass, "targetClass cannot be null. For untyped parsing use " + getClass().getName() + ".parse(String).");
		for (ParserEntry<?> entry : parserEntries)
		{
			if (targetClass.isAssignableFrom(entry.targetType))
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
		throw buildNoMatchException(text, targetClass);
	}

	private static NoMatchException buildNoMatchException(String text, Class<?> targetClass)
	{
		// build Exception message
		StringBuilder msg = new StringBuilder();
		msg.append("No parser ");
		if (targetClass != null)
		{
			msg.append("that produces objects of");
			msg.append(targetClass);
			msg.append(' ');
		}
		msg.append("could parse the text");

		throw new NoMatchException(text, targetClass, msg.toString());
	}

	public static final class ParserEntry<T>
	{
		private final Parser<T>	parser;
		private final Class<T>	targetType;

		public ParserEntry(Parser<T> parser, Class<T> targetType)
		{
			this.parser = Objects.requireNonNull(parser, "parser");
			this.targetType = Objects.requireNonNull(targetType, "parser");
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

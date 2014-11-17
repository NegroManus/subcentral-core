package de.subcentral.core.parsing;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * 
 * @implSpec #thread-safe
 *
 */
public class SimpleParsingService implements ParsingService
{
	private final String							domain;
	private final ListMultimap<Class<?>, Parser<?>>	parsers		= LinkedListMultimap.create();
	private final ReentrantReadWriteLock			parsersRwl	= new ReentrantReadWriteLock();

	public SimpleParsingService(String domain)
	{
		this.domain = Objects.requireNonNull(domain, "domain");
	}

	@Override
	public String getDomain()
	{
		return domain;
	}

	/**
	 * 
	 * @return an immutable copy of the current parsers map (a snapshot)
	 */
	public ListMultimap<Class<?>, Parser<?>> getParsers()
	{
		parsersRwl.readLock().lock();
		try
		{
			return ImmutableListMultimap.copyOf(parsers);
		}
		finally
		{
			parsersRwl.readLock().unlock();
		}
	}

	public <T> boolean registerParser(Class<T> targetClass, Parser<T> parser)
	{
		parsersRwl.writeLock().lock();
		try
		{
			return parsers.put(targetClass, parser);
		}
		finally
		{
			parsersRwl.writeLock().unlock();
		}
	}

	public <T> boolean registerAllParsers(Class<T> targetClass, Iterable<Parser<T>> parsers)
	{
		parsersRwl.writeLock().lock();
		try
		{
			return this.parsers.putAll(targetClass, parsers);
		}
		finally
		{
			parsersRwl.writeLock().unlock();
		}
	}

	public boolean registerAllParsers(ListMultimap<Class<?>, Parser<?>> parsers)
	{
		parsersRwl.writeLock().lock();
		try
		{
			return this.parsers.putAll(parsers);
		}
		finally
		{
			parsersRwl.writeLock().unlock();
		}
	}

	public <T> boolean unregisterParser(Class<T> targetClass, Parser<T> parser)
	{
		parsersRwl.writeLock().lock();
		try
		{

			return parsers.remove(targetClass, parser);
		}
		finally
		{
			parsersRwl.writeLock().unlock();
		}
	}

	public <T> List<Parser<?>> unregisterAllParsers(Class<T> targetClass)
	{

		parsersRwl.writeLock().lock();
		try
		{
			return parsers.removeAll(targetClass);
		}
		finally
		{
			parsersRwl.writeLock().unlock();
		}
	}

	public int unregisterAllParsers()
	{
		parsersRwl.writeLock().lock();
		try
		{
			int size = parsers.size();
			parsers.clear();
			return size;
		}
		finally
		{
			parsersRwl.writeLock().unlock();
		}
	}

	@Override
	public Object parse(String text) throws NoMatchException, ParsingException
	{
		return doParse(text, null);
	}

	@Override
	public <T> T parse(String text, Class<T> targetClass) throws NoMatchException, ParsingException
	{
		Parsings.requireNotBlank(text, targetClass);
		try
		{
			Objects.requireNonNull(targetClass, "targetClass cannot be null. For untyped parsing use " + getClass().getName() + ".parse(String).");
			return targetClass.cast(doParse(text, targetClass));
		}
		catch (ClassCastException | NullPointerException e)
		{
			throw new ParsingException(text, targetClass, e);
		}
	}

	private Object doParse(String text, Class<?> targetClass) throws NoMatchException, ParsingException
	{
		parsersRwl.readLock().lock();
		try
		{
			for (Parser<?> p : (targetClass == null ? parsers.values() : parsers.get(targetClass)))
			{
				try
				{
					return p.parse(text);
				}
				catch (NoMatchException e)
				{
					// this parser could no match
					// ignore and move on to the next
					continue;
				}
			}
		}
		finally
		{
			parsersRwl.readLock().unlock();
		}

		// build Exception message
		StringBuilder msg = new StringBuilder();
		msg.append("No parser ");
		if (targetClass != null)
		{
			msg.append("with ");
			msg.append("entity type");
			msg.append(targetClass);
			msg.append(' ');
		}
		msg.append("could parse the text");

		throw new NoMatchException(text, targetClass, msg.toString());
	}
}

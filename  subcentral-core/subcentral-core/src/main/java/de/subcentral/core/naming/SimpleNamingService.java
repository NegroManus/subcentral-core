package de.subcentral.core.naming;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ClassUtils;

public class SimpleNamingService implements NamingService
{
	private String					domain;
	private Map<Class<?>, Namer<?>>	namers	= new HashMap<>(6);

	@Override
	public String getDomain()
	{
		return domain;
	}

	public void setDomain(String domain)
	{
		this.domain = domain;
	}

	public Map<Class<?>, Namer<?>> getNamers()
	{
		return namers;
	}

	public void setNamers(Map<Class<?>, Namer<?>> namers) throws IllegalArgumentException
	{
		checkNamersMap(namers);
		this.namers = namers;
	}

	public <T> Namer<?> registerNamer(Namer<T> namer)
	{
		return registerNamer(namer.getType(), namer);
	}

	public <T> Namer<?> registerNamer(Class<T> typeClass, Namer<? super T> namer)
	{
		return namers.put(typeClass, namer);
	}

	public Namer<?> unregisterNamer(Class<?> typeClass)
	{
		return namers.remove(typeClass);
	}

	@Override
	public boolean canName(Object candidate)
	{
		if (candidate == null)
		{
			return false;
		}
		return getNamer(candidate.getClass()) != null;
	}

	public Namer<?> getNamer(Class<?> clazz)
	{
		Class<?> searchClass = clazz;
		while (searchClass != Object.class)
		{
			Namer<?> namer = namers.get(searchClass);
			if (namer != null)
			{
				return namer;
			}
			searchClass = searchClass.getSuperclass();
		}
		for (Class<?> interfaceClass : ClassUtils.getAllInterfaces(clazz))
		{
			Namer<?> namer = namers.get(interfaceClass);
			if (namer != null)
			{
				return namer;
			}
		}
		return null;
	}

	/**
	 * Returns a name for the given object which is calculated as follows:
	 * <ul>
	 * <li>1. If a namer is registered for the class of candidate (excluding Object), return namer.name(obj).</li>
	 * <li>2. If a namer is registered for a superclass of candidate (excluding Object), return namer.name(obj).</li>
	 * <li>3. If a namer is registered for an interface of candidate, return namer.name(obj).</li>
	 * <li>4. Throw NoNamerRegisteredException.</li>
	 * </ul>
	 * <p>
	 * <b>Note: Register the Namers to the concrete classes to ensure best performance. Searching for Namers of superclasses or interfaces is more
	 * costly. </b>
	 * </p>
	 * 
	 * @param candidate
	 *            The object to name. May be null.
	 * @param parameters
	 *            The naming parameters. Not null.
	 * @return The name that was determined for the object or null if the candidate was null.
	 */
	@Override
	public <T> String name(T candidate, Map<String, Object> parameters) throws NamingException, NoNamerRegisteredException
	{
		if (candidate == null)
		{
			return null;
		}
		@SuppressWarnings("unchecked")
		Namer<? super T> namer = (Namer<? super T>) getNamer(candidate.getClass());
		if (namer != null)
		{
			return namer.name(candidate, this, parameters);
		}
		throw new NoNamerRegisteredException(candidate);
	}

	private final void checkNamersMap(Map<Class<?>, Namer<?>> namers) throws IllegalArgumentException
	{
		for (Map.Entry<Class<?>, Namer<?>> entry : namers.entrySet())
		{
			if (!entry.getValue().getType().isAssignableFrom(entry.getKey()))
			{
				throw new IllegalArgumentException("The map contains an invalid entry: The namer's type class (" + entry.getValue().getType()
						+ ") is not assignable from the class: " + entry.getKey() + " (not a superclass or implemented interface of).");
			}
		}
	}
}
package de.subcentral.core.naming;

import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

import org.apache.commons.lang3.ClassUtils;

public class ClassBasedNamingService implements NamingService
{
	private String					domain;
	private Map<Class<?>, Namer<?>>	namers				= new HashMap<>();
	private UnaryOperator<String>	wholeNameOperator	= UnaryOperator.identity();

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
		this.namers = namers;
	}

	public <T> Namer<?> registerNamer(Class<T> typeClass, Namer<? super T> namer)
	{
		return namers.put(typeClass, namer);
	}

	public Namer<?> unregisterNamer(Class<?> typeClass)
	{
		return namers.remove(typeClass);
	}

	public UnaryOperator<String> getWholeNameOperator()
	{
		return wholeNameOperator;
	}

	public void setWholeNameOperator(UnaryOperator<String> wholeNameOperator)
	{
		this.wholeNameOperator = wholeNameOperator;
	}

	@Override
	public boolean canName(Object candidate)
	{
		return candidate != null && getNamer(candidate.getClass()) != null;
	}

	@SuppressWarnings("unchecked")
	public <T> Namer<? super T> getNamer(Class<T> clazz)
	{
		Class<?> searchClass = clazz;
		while (searchClass != Object.class)
		{
			Namer<?> namer = namers.get(searchClass);
			if (namer != null)
			{
				return (Namer<? super T>) namer;
			}
			searchClass = searchClass.getSuperclass();
		}
		for (Class<?> interfaceClass : ClassUtils.getAllInterfaces(clazz))
		{
			Namer<?> namer = namers.get(interfaceClass);
			if (namer != null)
			{
				return (Namer<? super T>) namer;
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
	 * @throws NoNamerRegisteredException
	 *             if no namer is registered for the candidate
	 * @throws NamingException
	 *             if an exception occurs while naming
	 */
	@Override
	public String name(Object candidate, Map<String, Object> parameters) throws NamingException
	{
		return doName(candidate, parameters);
	}

	private final <T> String doName(T candidate, Map<String, Object> parameters) throws NoNamerRegisteredException, NamingException
	{
		if (candidate == null)
		{
			return null;
		}
		@SuppressWarnings("unchecked")
		Namer<? super T> namer = (Namer<? super T>) getNamer(candidate.getClass());
		if (namer != null)
		{
			return wholeNameOperator.apply(namer.name(candidate, parameters));
		}
		throw new NoNamerRegisteredException(candidate);
	}
}

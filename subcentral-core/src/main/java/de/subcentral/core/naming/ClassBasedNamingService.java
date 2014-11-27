package de.subcentral.core.naming;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.ClassUtils;

/**
 * {@code Thread-safe}
 *
 */
public class ClassBasedNamingService implements NamingService
{
	private final String					domain;
	private final Map<Class<?>, Namer<?>>	namers				= new ConcurrentHashMap<>();
	private final AtomicReference<String>	defaultSeparator	= new AtomicReference<>(" ");

	public ClassBasedNamingService(String domain)
	{
		this.domain = Objects.requireNonNull(domain, "domain");
	}

	@Override
	public String getDomain()
	{
		return domain;
	}

	@SuppressWarnings("unchecked")
	public <T> Namer<? super T> registerNamer(Class<T> type, Namer<? super T> namer)
	{
		return (Namer<? super T>) namers.put(type, namer);
	}

	@SuppressWarnings("unchecked")
	public <T> Namer<? super T> unregisterNamer(Class<T> type)
	{
		return (Namer<? super T>) namers.remove(type);
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

	public Map<Class<?>, Namer<?>> getNamers()
	{
		return Collections.unmodifiableMap(namers);
	}

	@Override
	public String getDefaultSeparator()
	{
		return defaultSeparator.get();
	}

	public void setDefaultSeparator(String defaultSeparator)
	{
		this.defaultSeparator.set(Objects.requireNonNull(defaultSeparator, "defaultSeparator"));
	}

	@Override
	public boolean canName(Object candidate)
	{
		return candidate != null && getNamer(candidate.getClass()) != null;
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
	 * @return The name that was determined for the object or "" if the candidate was null.
	 * @throws NoNamerRegisteredException
	 *             if no namer is registered for the candidate
	 * @throws NamingException
	 *             if an exception occurs while naming
	 */
	@Override
	public String name(Object candidate, Map<String, Object> parameters) throws NoNamerRegisteredException, NamingException
	{
		if (candidate == null)
		{
			return "";
		}
		return doName(candidate, parameters);
	}

	private final <T> String doName(T candidate, Map<String, Object> parameters) throws NoNamerRegisteredException, NamingException
	{
		@SuppressWarnings("unchecked")
		Namer<? super T> namer = (Namer<? super T>) getNamer(candidate.getClass());
		if (namer != null)
		{
			return namer.name(candidate, parameters);
		}
		if (candidate instanceof Iterable)
		{
			return nameAll((Iterable<?>) candidate, parameters);
		}
		throw new NoNamerRegisteredException(candidate, "No Namer registered for candidate's type");
	}
}

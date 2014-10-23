package de.subcentral.core.naming;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

import org.apache.commons.lang3.ClassUtils;

/**
 * {@code Thread-safe}
 *
 */
public class ClassBasedNamingService implements NamingService
{
	private final String									domain;
	private final Map<Class<?>, Namer<?>>					namers				= new ConcurrentHashMap<>();
	private final AtomicReference<UnaryOperator<String>>	wholeNameOperator	= new AtomicReference<UnaryOperator<String>>(UnaryOperator.identity());

	public ClassBasedNamingService(String domain)
	{
		this.domain = Objects.requireNonNull(domain, "domain");
	}

	@Override
	public String getDomain()
	{
		return domain;
	}

	public Map<Class<?>, Namer<?>> getNamers()
	{
		return namers;
	}

	public UnaryOperator<String> getWholeNameOperator()
	{
		return wholeNameOperator.get();
	}

	public void setWholeNameOperator(UnaryOperator<String> wholeNameOperator)
	{
		this.wholeNameOperator.set(wholeNameOperator);
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
			return wholeNameOperator.get().apply(namer.name(candidate, parameters));
		}
		throw new NoNamerRegisteredException(candidate);
	}
}

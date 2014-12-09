package de.subcentral.core.naming;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import de.subcentral.core.util.Separation;

/**
 * {@code Thread-safe}
 */
public class ConditionalNamingService implements NamingService
{
	private final String					domain;
	private final List<ConditionalNamer<?>>	namers				= new CopyOnWriteArrayList<>();
	private final AtomicReference<String>	defaultSeparator	= new AtomicReference<>(Separation.DEFAULT_SEPARATOR);

	public ConditionalNamingService(String domain)
	{
		this.domain = Objects.requireNonNull(domain, "domain");
	}

	@Override
	public String getDomain()
	{
		return domain;
	}

	/**
	 * Important:
	 * <ul>
	 * <li>The order of the elements is the order the conditions are tested. So more restricting conditions must be placed before more general
	 * conditions. The first ConditionalNamer which condition returns true will be taken.</li>
	 * <li>The order should also consider how often specific types are named. The types that are named most frequently should be at the top of the
	 * list.</li>
	 * </ul>
	 * 
	 * @return
	 */
	public List<ConditionalNamer<?>> getNamers()
	{
		return namers;
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

	@SuppressWarnings("unchecked")
	public <T> Namer<? super T> getNamer(T candidate) throws ClassCastException
	{
		if (candidate == null)
		{
			return null;
		}
		for (ConditionalNamer<?> namer : namers)
		{
			if (namer.test(candidate))
			{
				return (Namer<? super T>) namer;
			}
		}
		return null;
	}

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
		Namer<? super T> namer = getNamer(candidate);
		if (namer != null)
		{
			return namer.name(candidate, parameters);
		}
		if (candidate instanceof Iterable)
		{
			return nameAll((Iterable<?>) candidate, parameters);
		}
		throw new NoNamerRegisteredException(candidate, "No ConditionalNamer's condition returned true for the candidate");
	}
}

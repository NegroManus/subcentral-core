package de.subcentral.core.naming;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * {@code Thread-safe}
 */
public class ConditionalNamingService implements NamingService
{
	private final String									domain;
	private final List<ConditionalNamer<?>>					namers				= new CopyOnWriteArrayList<>();
	private final AtomicReference<String>					defaultSeparator	= new AtomicReference<>(" ");
	private final AtomicReference<Function<String, String>>	finalFormatter		= new AtomicReference<>(Function.identity());

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
	 * conditions.</li>
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
		this.defaultSeparator.set(defaultSeparator);
	}

	public Function<String, String> getFinalFormatter()
	{
		return finalFormatter.get();
	}

	public void setFinalFormatter(Function<String, String> finalFormatter)
	{
		this.finalFormatter.set(finalFormatter);
	}

	@Override
	public boolean canName(Object candidate)
	{
		return getNamer(candidate) != null;
	}

	@SuppressWarnings("unchecked")
	public <T> Namer<? super T> getNamer(T candidate)
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
			return finalFormatter.get().apply(namer.name(candidate, parameters));
		}
		if (candidate instanceof Iterable)
		{
			return finalFormatter.get().apply(nameAll((Iterable<?>) candidate, defaultSeparator.get(), parameters));
		}
		throw new NoNamerRegisteredException(candidate, "No ConditionalNamer's condition returned true for the candidate");
	}
}
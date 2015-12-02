package de.subcentral.core.correction;

import java.util.Objects;
import java.util.function.Function;

public abstract class ReplacerCorrector<T, P> implements Corrector<T>
{
	protected final Function<P, P> replacer;

	public ReplacerCorrector(Function<P, P> replacer)
	{
		this.replacer = Objects.requireNonNull(replacer, "replacer");
	}

	public Function<P, P> getReplacer()
	{
		return replacer;
	}
}

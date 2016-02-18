package de.subcentral.core.correct;

import java.util.List;
import java.util.function.BiConsumer;

public interface Corrector<T> extends BiConsumer<T, List<Correction>>
{
	public void correct(T bean, List<Correction> corrections);

	@Override
	public default void accept(T bean, List<Correction> corrections)
	{
		correct(bean, corrections);
	}
}

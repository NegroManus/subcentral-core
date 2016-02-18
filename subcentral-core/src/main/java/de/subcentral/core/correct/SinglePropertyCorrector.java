package de.subcentral.core.correct;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public abstract class SinglePropertyCorrector<T, P> implements Corrector<T>
{
	protected final Function<P, P> replacer;

	public SinglePropertyCorrector(Function<P, P> replacer)
	{
		this.replacer = Objects.requireNonNull(replacer, "replacer");
	}

	public Function<P, P> getReplacer()
	{
		return replacer;
	}

	public abstract String getPropertyName();

	protected abstract P getValue(T bean);

	protected abstract void setValue(T bean, P value);

	@Override
	public void correct(T bean, List<Correction> changes)
	{
		P oldValue = getValue(bean);
		P newValue = replacer.apply(oldValue);
		if (!Objects.equals(oldValue, newValue))
		{
			P oldValueClone = cloneValue(oldValue);
			P newValueClone = cloneValue(newValue);
			setValue(bean, newValue);
			changes.add(new Correction(bean, getPropertyName(), oldValueClone, newValueClone, this));
		}
	}

	/**
	 * In case of a correction the old value and new value have to be cloned because to either one of those external references could exist that would allow the values to be changed and the correction
	 * entry to be altered.
	 * <p>
	 * The default implementation just returns the value. This is okay for all immutable values like Strings and the wrapper classes.
	 * </p>
	 * 
	 * @param value
	 *            the value to clone
	 * @return the cloned value
	 */
	protected P cloneValue(P value)
	{
		return value;
	}
}

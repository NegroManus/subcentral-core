package de.subcentral.core.correction;

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

	/**
	 * If the property value is mutable, this methods needs to return a fresh copy, so that is not the same object as the one that will be modified via {@link #setValue(Object, Object)}.
	 * 
	 * @param bean
	 * @return the current property value
	 */
	protected abstract P getValue(T bean);

	protected abstract void setValue(T bean, P value);

	@Override
	public void correct(T bean, List<Correction> changes)
	{
		P oldValue = getValue(bean);
		P newValue = replacer.apply(oldValue);
		if (!Objects.equals(oldValue, newValue))
		{
			setValue(bean, newValue);
			changes.add(new Correction(bean, getPropertyName(), cloneValue(oldValue), cloneValue(newValue)));
		}
	}

	protected P cloneValue(P value)
	{
		return value;
	}
}

package de.subcentral.core.correction;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import com.google.common.base.MoreObjects;

public abstract class SinglePropertyCorrector<T, P> implements Corrector<T>
{
	private final Function<P, P> replacer;

	public SinglePropertyCorrector(Function<P, P> replacer)
	{
		this.replacer = Objects.requireNonNull(replacer, "replacer");
	}

	public abstract Class<T> getBeanType();

	public abstract String getPropertyName();

	public Function<P, P> getReplacer()
	{
		return replacer;
	}

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
			changes.add(new Correction(bean, getPropertyName(), oldValue, newValue));
			setValue(bean, newValue);
		}
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(this.getClass()).add("replacer", replacer).toString();
	}
}

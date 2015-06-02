package de.subcentral.core.standardizing;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public abstract class SinglePropertyStandardizer<T, P> implements Standardizer<T>
{
	private final Function<P, P>	replacer;

	public SinglePropertyStandardizer(Function<P, P> replacer)
	{
		this.replacer = Objects.requireNonNull(replacer, "replacer");
	}

	public Function<P, P> getReplacer()
	{
		return replacer;
	}

	public abstract Class<T> getBeanType();

	public abstract String getPropertyName();

	protected abstract P getValue(T bean);

	protected abstract void setValue(T bean, P value);

	@Override
	public void standardize(T bean, List<StandardizingChange> changes)
	{
		P oldValue = getValue(bean);
		P newValue = replacer.apply(oldValue);
		if (!Objects.equals(oldValue, newValue))
		{
			changes.add(new StandardizingChange(bean, getPropertyName(), oldValue, newValue));
			setValue(bean, newValue);
		}
	}
}

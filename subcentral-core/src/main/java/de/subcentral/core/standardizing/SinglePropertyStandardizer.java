package de.subcentral.core.standardizing;

import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

public abstract class SinglePropertyStandardizer<T, P, R extends UnaryOperator<P>> implements Standardizer<T>
{
	private final R	replacer;

	public SinglePropertyStandardizer(R replacer)
	{
		this.replacer = Objects.requireNonNull(replacer, "replacer");
	}

	public abstract Class<T> getBeanType();

	public abstract String getPropertyName();

	public R getReplacer()
	{
		return replacer;
	}

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

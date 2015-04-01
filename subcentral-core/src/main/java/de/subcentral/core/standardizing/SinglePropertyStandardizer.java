package de.subcentral.core.standardizing;

import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

public abstract class SinglePropertyStandardizer<T, P, R extends UnaryOperator<P>> implements Standardizer<T>
{
	private final Class<T>	beanType;
	private final String	propertyName;
	private final R			replacer;

	public SinglePropertyStandardizer(Class<T> beanType, String propertyName, R replacer)
	{
		this.beanType = Objects.requireNonNull(beanType, "beanType");
		this.propertyName = Objects.requireNonNull(propertyName, "propertyName");
		this.replacer = Objects.requireNonNull(replacer, "replacer");
	}

	public Class<T> getBeanType()
	{
		return beanType;
	}

	public String getPropertyName()
	{
		return propertyName;
	}

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
			changes.add(new StandardizingChange(bean, propertyName, oldValue, newValue));
			setValue(bean, newValue);
		}
	}
}

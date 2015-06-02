package de.subcentral.core.standardizing;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class ReflectiveStandardizer<T, P> implements Standardizer<T>
{
	private final Class<T>			beanType;
	private final String			propertyName;
	private final Function<P, P>	replacer;

	public ReflectiveStandardizer(Class<T> beanType, String propertyName, Function<P, P> replacer)
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

	public Function<?, ?> getReplacer()
	{
		return replacer;
	}

	@Override
	public void standardize(T bean, List<StandardizingChange> changes) throws IllegalArgumentException
	{
		if (bean == null)
		{
			return;
		}
		StandardizingChange change = standardizeProperty(bean, propertyName, replacer);
		if (change != null)
		{
			changes.add(change);
		}
	}

	@SuppressWarnings("unchecked")
	private StandardizingChange standardizeProperty(Object bean, String propName, Function<P, P> operator) throws IllegalArgumentException
	{
		try
		{
			PropertyDescriptor propDescr = new PropertyDescriptor(propName, bean.getClass());
			P oldVal = (P) propDescr.getReadMethod().invoke(bean);
			P newVal = operator.apply(oldVal);
			if (Objects.equals(oldVal, newVal))
			{
				return null;
			}
			propDescr.getWriteMethod().invoke(bean, newVal);
			return new StandardizingChange(bean, propName, oldVal, newVal);
		}
		catch (IntrospectionException | IllegalAccessException | InvocationTargetException | RuntimeException e)
		{
			throw new IllegalArgumentException("Exception while standardizing property " + bean.getClass().getName() + "." + propName + " of bean "
					+ bean + " with operator " + operator, e);
		}
	}
}

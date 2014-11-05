package de.subcentral.core.standardizing;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class ReflectiveStandardizer<T> implements Standardizer<T>
{
	private final Class<T>						beanType;
	private final Map<String, UnaryOperator<?>>	propStandardizers;

	public ReflectiveStandardizer(Class<T> beanType, Map<String, UnaryOperator<?>> propStandardizers)
	{
		this.beanType = Objects.requireNonNull(beanType, "beanType");
		this.propStandardizers = ImmutableMap.copyOf(propStandardizers); // null check included
	}

	public Class<T> getBeanType()
	{
		return beanType;
	}

	public Map<String, UnaryOperator<?>> getPropStandardizers()
	{
		return propStandardizers;
	}

	@Override
	public List<StandardizingChange> standardize(T bean) throws StandardizingException
	{
		if (bean == null)
		{
			return ImmutableList.of();
		}
		ImmutableList.Builder<StandardizingChange> changes = ImmutableList.builder();
		for (Map.Entry<String, UnaryOperator<?>> entry : propStandardizers.entrySet())
		{
			StandardizingChange change = standardizeProperty(bean, entry.getKey(), entry.getValue());
			if (change != null)
			{
				changes.add(change);
			}
		}
		return changes.build();
	}

	@SuppressWarnings("unchecked")
	private <P> StandardizingChange standardizeProperty(Object bean, String propName, UnaryOperator<P> operator) throws StandardizingException
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
			throw new StandardizingException("Exception while standardizing property " + bean.getClass().getName() + "." + propName + " of bean "
					+ bean + " with operator " + operator, e);
		}
	}
}

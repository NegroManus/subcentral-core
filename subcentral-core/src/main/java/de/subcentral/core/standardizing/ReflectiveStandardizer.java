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

import de.subcentral.core.util.SimplePropDescriptor;

public class ReflectiveStandardizer<T> implements Standardizer<T>
{
	private final Class<T>						beanClass;
	private final Map<String, UnaryOperator<?>>	propStandardizers;

	public ReflectiveStandardizer(Class<T> beanClass, Map<String, UnaryOperator<?>> propStandardizers)
	{
		this.beanClass = Objects.requireNonNull(beanClass, "beanClass");
		this.propStandardizers = ImmutableMap.copyOf(propStandardizers); // null check included
	}

	public Class<T> getBeanClass()
	{
		return beanClass;
	}

	public Map<String, UnaryOperator<?>> getPropStandardizers()
	{
		return propStandardizers;
	}

	@Override
	public List<StandardizingChange> standardize(T bean)
	{
		if (bean == null)
		{
			return ImmutableList.of();
		}
		ImmutableList.Builder<StandardizingChange> changes = ImmutableList.builder();
		for (Map.Entry<String, UnaryOperator<?>> entry : propStandardizers.entrySet())
		{
			try
			{
				StandardizingChange change = standardizeProperty(bean, entry.getKey(), entry.getValue());
				if (change != null)
				{
					changes.add(change);
				}
			}
			catch (IntrospectionException | ClassCastException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
			{
				e.printStackTrace();
			}
		}
		return changes.build();
	}

	@SuppressWarnings("unchecked")
	private <P> StandardizingChange standardizeProperty(Object bean, String prop, UnaryOperator<P> operator) throws IntrospectionException,
			ClassCastException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		PropertyDescriptor propDescr = new PropertyDescriptor(prop, bean.getClass());
		P oldVal = (P) propDescr.getReadMethod().invoke(bean);
		P newVal = operator.apply(oldVal);
		if (Objects.equals(oldVal, newVal))
		{
			return null;
		}
		propDescr.getWriteMethod().invoke(bean, newVal);
		return new StandardizingChange(bean, new SimplePropDescriptor(bean.getClass(), prop), oldVal, newVal);
	}
}

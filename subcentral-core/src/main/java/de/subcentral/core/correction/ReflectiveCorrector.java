package de.subcentral.core.correction;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class ReflectiveCorrector<T, P> implements Corrector<T>
{
	private final Class<T>			beanType;
	private final String			propertyName;
	private final Function<P, P>	replacer;

	public ReflectiveCorrector(Class<T> beanType, String propertyName, Function<P, P> replacer)
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
	public void correct(T bean, List<Correction> corrections) throws IllegalArgumentException
	{
		if (bean == null)
		{
			return;
		}
		Correction change = standardizeProperty(bean, propertyName, replacer);
		if (change != null)
		{
			corrections.add(change);
		}
	}

	@SuppressWarnings("unchecked")
	private Correction standardizeProperty(Object bean, String propName, Function<P, P> operator) throws IllegalArgumentException
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
			return new Correction(bean, propName, oldVal, newVal);
		}
		catch (IntrospectionException | IllegalAccessException | InvocationTargetException | RuntimeException e)
		{
			throw new IllegalArgumentException("Exception while standardizing property " + bean.getClass().getName() + "." + propName + " of bean " + bean + " with operator " + operator, e);
		}
	}
}

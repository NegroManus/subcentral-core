package de.subcentral.core.correct;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.function.Function;

import com.google.common.base.MoreObjects;

import de.subcentral.core.util.SimplePropDescriptor;

public class ReflectiveCorrector<T, P> extends SinglePropertyCorrector<T, P>
{
	private final PropertyDescriptor	propertyDescriptor;
	private final Function<P, P>		cloner;

	public ReflectiveCorrector(SimplePropDescriptor simplePropDescriptor, Function<P, P> replacer, Function<P, P> cloner) throws IntrospectionException
	{
		this(simplePropDescriptor.toPropertyDescriptor(), replacer, cloner);
	}

	public ReflectiveCorrector(Class<T> beanType, String propertyName, Function<P, P> replacer, Function<P, P> cloner) throws IntrospectionException
	{
		this(new PropertyDescriptor(propertyName, beanType), replacer, cloner);
	}

	public ReflectiveCorrector(PropertyDescriptor propertyDescriptor, Function<P, P> replacer, Function<P, P> cloner)
	{
		super(replacer);
		this.propertyDescriptor = Objects.requireNonNull(propertyDescriptor, "propertyDescriptor");
		this.cloner = Objects.requireNonNull(cloner, "cloner");
	}

	public PropertyDescriptor getPropertyDescriptor()
	{
		return propertyDescriptor;
	}

	public Function<P, P> getCloner()
	{
		return cloner;
	}

	@Override
	public String getPropertyName()
	{
		return propertyDescriptor.getName();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected P getValue(T bean)
	{
		try
		{
			return (P) propertyDescriptor.getReadMethod().invoke(bean);
		}
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void setValue(T bean, P value)
	{
		try
		{
			propertyDescriptor.getWriteMethod().invoke(bean, value);
		}
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	protected P cloneValue(P value)
	{
		return cloner.apply(value);
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(this.getClass()).add("propertyDescriptor", propertyDescriptor).add("replacer", replacer).toString();
	}
}

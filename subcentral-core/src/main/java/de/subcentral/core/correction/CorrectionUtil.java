package de.subcentral.core.correction;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.function.BiConsumer;
import java.util.function.Function;

import de.subcentral.core.util.SimplePropDescriptor;

public class CorrectionUtil
{
	public static <T, P> Corrector<T> newReflectiveCorrector(SimplePropDescriptor simplePropDescriptor, Function<P, P> replacer) throws IntrospectionException
	{
		return newReflectiveCorrector(simplePropDescriptor.toPropertyDescriptor(), replacer);
	}

	public static <T, P> Corrector<T> newReflectiveCorrector(Class<T> beanType, String propertyName, Function<P, P> replacer) throws IntrospectionException
	{
		return newReflectiveCorrector(new PropertyDescriptor(propertyName, beanType), replacer);
	}

	@SuppressWarnings("unchecked")
	public static <T, P> Corrector<T> newReflectiveCorrector(PropertyDescriptor propDescriptor, Function<P, P> replacer)
	{
		final Function<T, P> getter = (T bean) ->
		{
			try
			{
				return (P) propDescriptor.getReadMethod().invoke(bean);
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		};
		final BiConsumer<T, P> setter = (T bean, P newValue) ->
		{
			try
			{
				propDescriptor.getWriteMethod().invoke(bean, newValue);
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		};

		return new FunctionalCorrector<>(propDescriptor.getName(), getter, setter, replacer);
	}
}

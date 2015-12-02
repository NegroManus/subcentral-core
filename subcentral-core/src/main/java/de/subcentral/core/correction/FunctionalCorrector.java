package de.subcentral.core.correction;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.google.common.base.MoreObjects;

public final class FunctionalCorrector<T, P> extends SinglePropertyCorrector<T, P>
{
	private final String			propertyName;
	private final Function<T, P>	getter;
	private final BiConsumer<T, P>	setter;

	public FunctionalCorrector(String propertyName, Function<T, P> getter, BiConsumer<T, P> setter, Function<P, P> replacer)
	{
		super(replacer);
		this.propertyName = Objects.requireNonNull(propertyName, "propertyName");
		this.getter = Objects.requireNonNull(getter, "getter");
		this.setter = Objects.requireNonNull(setter, "setter");
	}

	@Override
	public String getPropertyName()
	{
		return propertyName;
	}

	@Override
	protected P getValue(T bean)
	{
		return getter.apply(bean);
	}

	@Override
	protected void setValue(T bean, P value)
	{
		setter.accept(bean, value);
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(this.getClass()).add("propertyName", propertyName).add("replacer", replacer).toString();
	}
}
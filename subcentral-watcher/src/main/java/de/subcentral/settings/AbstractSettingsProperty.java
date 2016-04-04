package de.subcentral.settings;

import java.util.Objects;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.Property;

public abstract class AbstractSettingsProperty<T, P extends Property<T>> implements SettingsProperty<T, P>
{
	protected P					original	= createProperty("original");
	protected P					current		= createProperty("current");
	protected BooleanBinding	changed		= createChangedBinding();
	protected final String		key;

	public AbstractSettingsProperty(String key)
	{
		this.key = Objects.requireNonNull(key, "key");
	}

	protected abstract P createProperty(String name);

	protected BooleanBinding createChangedBinding()
	{
		return new BooleanBinding()
		{
			{
				super.bind(original, current);
			}

			@Override
			protected boolean computeValue()
			{
				return Objects.equals(original.getValue(), current.getValue());
			}
		};
	}

	@Override
	public P currentProperty()
	{
		return current;
	}

	@Override
	public P originalProperty()
	{
		return original;
	}

	@Override
	public T getOriginal()
	{
		return original.getValue();
	}

	@Override
	public void setOriginal(T value)
	{
		original.setValue(value);
	}

	@Override
	public T getCurrent()
	{
		return current.getValue();
	}

	@Override
	public void setCurrent(T value)
	{
		current.setValue(value);
	}

	@Override
	public BooleanBinding changedBinding()
	{
		return changed;
	}

	@Override
	public boolean hasChanged()
	{
		return changed.get();
	}

	@Override
	public void reset()
	{
		current.setValue(original.getValue());
	}

	public String getKey()
	{
		return key;
	}
}

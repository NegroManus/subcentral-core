package de.subcentral.settings;

import org.apache.commons.configuration2.XMLConfiguration;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class BooleanSettingsProperty extends SettingsPropertyBase<Boolean, BooleanProperty>
{
	private final boolean defaultValue;

	public BooleanSettingsProperty(String key, boolean defaultValue)
	{
		super(key);
		this.defaultValue = defaultValue;
	}

	@Override
	protected BooleanProperty createProperty(String name)
	{
		return new SimpleBooleanProperty(this, name, defaultValue);
	}

	@Override
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
				return original.get() == current.get();
			}
		};
	}

	public boolean getOriginalBoolean()
	{
		return original.get();
	}

	public void setOriginalBoolean(boolean value)
	{
		original.set(value);
	}

	public boolean getCurrentBoolean()
	{
		return current.get();
	}

	public void setCurrentBoolean(boolean value)
	{
		current.set(value);
	}

	@Override
	public void reset()
	{
		current.set(original.get());
	}

	@Override
	public void load(XMLConfiguration cfg)
	{
		boolean val = cfg.getBoolean(key, defaultValue);
		original.set(val);
		current.set(val);
	}

	@Override
	public void save(XMLConfiguration cfg)
	{
		cfg.addProperty(key, current);
	}

	public boolean getDefaultValue()
	{
		return defaultValue;
	}
}

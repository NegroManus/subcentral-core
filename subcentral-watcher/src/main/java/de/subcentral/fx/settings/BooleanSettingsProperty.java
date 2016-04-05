package de.subcentral.fx.settings;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class BooleanSettingsProperty extends SettingsPropertyBase<Boolean, BooleanProperty>
{
	private static final Logger		log	= LogManager.getLogger(ObjectSettingsPropertyBase.class);

	private final boolean			defaultValue;
	private boolean					original;
	private BooleanProperty			current;
	private final BooleanBinding	changed;

	public BooleanSettingsProperty(String key, boolean defaultValue)
	{
		super(key);
		this.defaultValue = defaultValue;
		this.original = defaultValue;
		this.current = new SimpleBooleanProperty(this, "current", defaultValue);
		this.changed = new BooleanBinding()
		{
			{
				this.bind(current);
			}

			@Override
			protected boolean computeValue()
			{
				return original == current.get();
			}
		};
	}

	public boolean getDefaultValue()
	{
		return defaultValue;
	}

	@Override
	public Boolean getOriginal()
	{
		return original;
	}

	public boolean getOriginalBoolean()
	{
		return original;
	}

	public void setOriginalBoolean(boolean value)
	{
		original = value;
	}

	@Override
	public BooleanProperty currentProperty()
	{
		return current;
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
	public BooleanBinding changedBinding()
	{
		return changed;
	}

	@Override
	public void reset()
	{
		current.set(original);
	}

	@Override
	public void load(XMLConfiguration cfg)
	{
		boolean val;
		try
		{
			val = cfg.getBoolean(key, defaultValue);
		}
		catch (Exception e)
		{
			log.error("Exception while loading value for boolean settings property " + key + ". Using default value: " + defaultValue, e);
			val = defaultValue;
		}
		original = val;
		current.set(val);
	}

	@Override
	public void save(XMLConfiguration cfg)
	{
		cfg.setProperty(key, current);
	}
}

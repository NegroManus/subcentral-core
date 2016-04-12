package de.subcentral.fx.settings;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class BooleanSettingsProperty extends SettingsPropertyBase<Boolean, BooleanProperty>
{
	private static final Logger		log	= LogManager.getLogger(ObjectSettingsPropertyBase.class);

	private final boolean			defaultValue;
	private boolean					lastSaved;
	private BooleanProperty			current;
	private final BooleanBinding	changedBinding;

	public BooleanSettingsProperty(String key, boolean defaultValue)
	{
		super(key);
		this.defaultValue = defaultValue;
		lastSaved = defaultValue;
		current = new SimpleBooleanProperty(this, "current", defaultValue);
		helper.getDependencies().add(current);
		changedBinding = (new BooleanBinding()
		{
			{
				super.bind(helper);
			}

			@Override
			protected boolean computeValue()
			{
				return lastSaved != current.get();
			}
		});
		changed.bind(changedBinding);
	}

	public boolean getDefaultValue()
	{
		return defaultValue;
	}

	@Override
	public Boolean getLastSaved()
	{
		return lastSaved;
	}

	public boolean getLastSavedBoolean()
	{
		return lastSaved;
	}

	public void setLastSavedBoolean(boolean value)
	{
		lastSaved = value;
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
	public void reset()
	{
		current.set(lastSaved);
	}

	@Override
	public void load(ImmutableConfiguration cfg)
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
		lastSaved = val;
		current.set(val);
		// Invalidate because lastSaved has changed
		// and setting of current may not have caused PropertyChangeEvent if old == new.
		changedBinding.invalidate();
	}

	@Override
	public void save(Configuration cfg)
	{
		boolean val = current.get();
		cfg.setProperty(key, val);
		lastSaved = val;
		// invalidate because lastSaved has changed
		changedBinding.invalidate();
	}
}

package de.subcentral.fx.settings;

import java.util.Objects;

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
	private final BooleanBinding	changedBinding;

	public BooleanSettingsProperty(String key, boolean defaultValue)
	{
		super(key);
		this.defaultValue = defaultValue;
		original = defaultValue;
		current = new SimpleBooleanProperty(this, "current", defaultValue);
		changedBinding = (new BooleanBinding()
		{
			{
				super.bind(current);
			}

			@Override
			protected boolean computeValue()
			{
				return Objects.equals(original, current.getValue());
			}
		});
		changed.bind(changedBinding);
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
		// Invalidate because original has changed
		// and setting of current may not have caused PropertyChangeEvent if old == new.
		changedBinding.invalidate();
	}

	@Override
	public void save(XMLConfiguration cfg)
	{
		boolean val = current.get();
		cfg.setProperty(key, val);
		original = val;
		// invalidate because original has changed
		changedBinding.invalidate();
	}
}

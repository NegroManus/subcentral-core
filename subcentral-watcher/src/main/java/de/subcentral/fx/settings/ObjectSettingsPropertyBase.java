package de.subcentral.fx.settings;

import java.util.Objects;
import java.util.function.BiFunction;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.subcentral.core.util.TriConsumer;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.Property;

public abstract class ObjectSettingsPropertyBase<T, P extends Property<T>> extends SettingsPropertyBase<T, P>
{
	private static final Logger								log	= LogManager.getLogger(ObjectSettingsPropertyBase.class);

	private final T											defaultValue;
	private T												original;
	private final P											current;
	private final BooleanBinding							changedBinding;
	private final BiFunction<XMLConfiguration, String, T>	loader;
	private final TriConsumer<XMLConfiguration, String, T>	saver;

	public ObjectSettingsPropertyBase(String key, T defaultValue, BiFunction<XMLConfiguration, String, T> loader, TriConsumer<XMLConfiguration, String, T> saver)
	{
		super(key);
		this.defaultValue = defaultValue;
		original = defaultValue;
		current = createProperty(this, "current", defaultValue);
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
		this.loader = Objects.requireNonNull(loader, "loader");
		this.saver = Objects.requireNonNull(saver, "saver");
	}

	protected abstract P createProperty(Object bean, String name, T initialValue);

	public T getDefaultValue()
	{
		return defaultValue;
	}

	@Override
	public T getOriginal()
	{
		return original;
	}

	@Override
	public P currentProperty()
	{
		return current;
	}

	@Override
	public void load(XMLConfiguration cfg)
	{
		T val;
		try
		{
			val = loader.apply(cfg, key);
		}
		catch (Exception e)
		{
			log.error("Exception while loading value for settings property " + key + ". Using default value: " + defaultValue, e);
			val = defaultValue;
		}
		original = val;
		current.setValue(val);
		// Invalidate because original has changed
		// and setting of current may not have caused PropertyChangeEvent if old == new.
		changedBinding.invalidate();
	}

	@Override
	public void save(XMLConfiguration cfg)
	{
		T val = current.getValue();
		saver.accept(cfg, key, val);
		original = val;
		// invalidate because original has changed
		changedBinding.invalidate();
	}
}

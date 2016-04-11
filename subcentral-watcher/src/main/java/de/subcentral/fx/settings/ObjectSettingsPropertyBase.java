package de.subcentral.fx.settings;

import java.util.Objects;
import java.util.function.Function;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.beans.Observable;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.Property;

public abstract class ObjectSettingsPropertyBase<T, P extends Property<T>> extends SettingsPropertyBase<T, P>
{
	private static final Logger						log	= LogManager.getLogger(ObjectSettingsPropertyBase.class);

	private final T									defaultValue;
	private T										original;
	private final P									current;
	private final BooleanBinding					changedBinding;
	private final ConfigurationPropertyHandler<T>	handler;

	protected ObjectSettingsPropertyBase(String key, ConfigurationPropertyHandler<T> handler, T defaultValue, Function<P, Observable> observablePropertyCreator)
	{
		super(key);
		this.handler = Objects.requireNonNull(handler, "handler");
		this.defaultValue = defaultValue;
		original = defaultValue;
		current = createProperty(this, "current", defaultValue);
		Observable currentObsv = observablePropertyCreator != null ? observablePropertyCreator.apply(current) : current;
		helper.getDependencies().add(currentObsv);
		changedBinding = (new BooleanBinding()
		{
			{
				super.bind(helper);
			}

			@Override
			protected boolean computeValue()
			{
				return Objects.equals(original, current.getValue());
			}
		});
		changed.bind(changedBinding);
	}

	protected abstract P createProperty(Object bean, String name, T initialValue);

	protected Observable[] getCurrentObservables()
	{
		return new Observable[] { current };
	}

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
	public void load(ImmutableConfiguration cfg)
	{
		T val;
		try
		{
			val = handler.get(cfg, key);
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
	public void save(Configuration cfg)
	{
		T val = current.getValue();
		handler.add(cfg, key, val);
		original = val;
		// invalidate because original has changed
		changedBinding.invalidate();
	}
}

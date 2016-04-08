package de.subcentral.fx.settings;

import java.util.Objects;

import de.subcentral.fx.ObservableHelper;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public abstract class SettingsPropertyBase<T, P extends Property<T>> implements SettingsProperty<T, P>
{
	protected final ObservableHelper	helper	= new ObservableHelper(this);
	protected final BooleanProperty		changed	= new SimpleBooleanProperty(this, "changed");
	protected final String				key;

	public SettingsPropertyBase(String key)
	{
		this.key = Objects.requireNonNull(key, "key");
	}

	@Override
	public void addListener(InvalidationListener listener)
	{
		helper.addListener(listener);
	}

	@Override
	public void removeListener(InvalidationListener listener)
	{
		helper.removeListener(listener);
	}

	@Override
	public ReadOnlyBooleanProperty changedProperty()
	{
		return changed;
	}

	public String getKey()
	{
		return key;
	}
}

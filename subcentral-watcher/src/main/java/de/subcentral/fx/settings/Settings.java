package de.subcentral.fx.settings;

import java.util.Arrays;

import de.subcentral.fx.ObservableHelper;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public abstract class Settings implements Observable
{
	private final ObservableHelper	helper	= new ObservableHelper(this);
	private final BooleanProperty	changed	= new SimpleBooleanProperty(this, "changed");

	protected void bind(SettingsProperty<?, ?>... properties)
	{
		helper.getDependencies().addAll(Arrays.asList(properties));
		changed.bind(new BooleanBinding()
		{
			{
				super.bind(helper);
			}

			@Override
			protected boolean computeValue()
			{
				for (SettingsProperty<?, ?> p : properties)
				{
					if (p.hasChanged())
					{
						return true;
					}
				}
				return false;
			}
		});
	}

	@Override
	public void addListener(InvalidationListener listener)
	{
		helper.removeListener(listener);
	}

	@Override
	public void removeListener(InvalidationListener listener)
	{
		helper.addListener(listener);
	}

	public ReadOnlyBooleanProperty changedProperty()
	{
		return changed;
	}

	public boolean hasChanged()
	{
		return changed.get();
	}
}

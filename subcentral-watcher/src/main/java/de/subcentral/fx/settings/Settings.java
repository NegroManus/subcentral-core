package de.subcentral.fx.settings;

import de.subcentral.fx.ObservableHelper;
import javafx.beans.Observable;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public abstract class Settings implements Observable
{
	private final ObservableHelper	helper	= new ObservableHelper(this);
	private final BooleanProperty	changed	= new SimpleBooleanProperty(this, "changed");

	protected void bindChanged(SettingsProperty<?, ?>... properties)
	{
		Observable[] changedObservables = new Observable[properties.length];
		ReadOnlyBooleanProperty[] changedProperties = new ReadOnlyBooleanProperty[properties.length];
		for (int i = 0; i < properties.length; i++)
		{
			ReadOnlyBooleanProperty p = properties[i].changedProperty();
			changedObservables[i] = p;
			changedProperties[i] = p;
		}
		changed.bind(new BooleanBinding()
		{
			{
				super.bind(changedProperties);
			}

			@Override
			protected boolean computeValue()
			{
				for (ReadOnlyBooleanProperty p : changedProperties)
				{
					if (p.get())
					{
						return true;
					}
				}
				return false;
			}
		});
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

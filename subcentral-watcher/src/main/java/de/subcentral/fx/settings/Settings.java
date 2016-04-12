package de.subcentral.fx.settings;

import java.util.List;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ImmutableConfiguration;

import com.google.common.collect.ImmutableList;

import de.subcentral.fx.ObservableHelper;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public abstract class Settings extends SettableBase implements Settable
{
	private List<Settable>			settables;
	private final ObservableHelper	helper	= new ObservableHelper(this);
	private final BooleanProperty	changed	= new SimpleBooleanProperty(this, "changed");

	{
		addListener((Observable o) ->
		{
			System.out.println(this.getClass().getSimpleName() + " changed");
		});
		changed.addListener(new ChangeListener<Boolean>()
		{

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
			{
				System.out.println(observable + " changed: " + oldValue + " -> " + newValue);
			}
		});
	}

	protected void initSettables(Settable... settables)
	{
		this.settables = ImmutableList.copyOf(settables);
		bindToSettables();
	}

	protected void initSettables(Iterable<? extends Settable> settables)
	{
		this.settables = ImmutableList.copyOf(settables);
		bindToSettables();
	}

	private void bindToSettables()
	{
		helper.getDependencies().addAll(settables);
		changed.bind(new BooleanBinding()
		{
			{
				super.bind(helper);
			}

			@Override
			protected boolean computeValue()
			{
				for (Settable p : settables)
				{
					if (p.changed())
					{
						return true;
					}
				}
				return false;
			}
		});
	}

	public List<Settable> getSettables()
	{
		return settables;
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

	@Override
	public boolean changed()
	{
		return changed.get();
	}

	@Override
	public void load(ImmutableConfiguration cfg)
	{
		for (Settable s : settables)
		{
			s.load(cfg);
		}
	}

	@Override
	public void save(Configuration cfg)
	{
		for (Settable s : settables)
		{
			s.save(cfg);
		}
	}

	@Override
	public void reset()
	{
		for (Settable s : settables)
		{
			s.reset();
		}
	}
}

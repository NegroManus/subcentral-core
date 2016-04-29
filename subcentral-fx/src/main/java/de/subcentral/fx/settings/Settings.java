package de.subcentral.fx.settings;

import java.util.List;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ImmutableConfiguration;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyBooleanProperty;

public class Settings extends SettableBase implements Settable
{
	private List<Settable> settables;

	{
		// TODO just for debug, remove!
		// helper.addListener((Observable o) ->
		// {
		// System.out.println(this.getClass().getSimpleName() + " invalidated");
		// });
		// changed.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) ->
		// {
		// System.out.println(this.getClass().getSimpleName() + " changed: " + oldValue + " -> " + newValue);
		// });
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
		ReadOnlyBooleanProperty[] changedProps = new ReadOnlyBooleanProperty[settables.size()];
		for (int i = 0; i < changedProps.length; i++)
		{
			changedProps[i] = settables.get(i).changedProperty();
		}
		changed.bind(new BooleanBinding()
		{
			{
				super.bind(changedProps);
			}

			@Override
			protected boolean computeValue()
			{
				for (Settable s : settables)
				{
					if (s.changed())
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
	public void load(ImmutableConfiguration cfg, boolean resetChanged)
	{
		for (Settable s : settables)
		{
			s.load(cfg, resetChanged);
		}
	}

	@Override
	public void save(Configuration cfg, boolean resetChanged)
	{
		for (Settable s : settables)
		{
			s.save(cfg, resetChanged);
		}
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(Settings.class).add("settables", settables).toString();
	}
}

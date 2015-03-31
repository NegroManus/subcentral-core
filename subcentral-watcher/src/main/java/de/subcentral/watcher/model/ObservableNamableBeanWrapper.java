package de.subcentral.watcher.model;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.util.TimeUtil;

public class ObservableNamableBeanWrapper<T> extends ObservableNamedBeanWrapper<T>
{
	private final StringBinding		name;
	private final Property<String>	actualName			= new SimpleStringProperty(this, "actualName");
	private final BooleanProperty	preferActualName	= new SimpleBooleanProperty(this, "preferActualName");

	public ObservableNamableBeanWrapper(T bean, NamingService namingService)
	{
		super(bean, namingService);
		name = new StringBinding()
		{
			{
				super.bind(computedName, actualName, preferActualName);
			}

			@Override
			protected String computeValue()
			{
				long start = System.nanoTime();
				String nameValue;
				if (preferActualName.get() && actualName.getValue() != null)
				{
					nameValue = actualName.getValue();
				}
				else
				{
					nameValue = computedName.getValue();
				}
				TimeUtil.printDurationMillis("NamableBean.name.computeValue(): " + nameValue, start);
				return nameValue;
			}
		};
	}

	@Override
	public String getName()
	{
		return name.getValue();
	}

	@Override
	public StringBinding nameBinding()
	{
		return name;
	}

	public final String getActualName()
	{
		return actualName.getValue();
	}

	public final void setActualName(final String actualName)
	{
		this.actualName.setValue(actualName);
	}

	public final Property<String> actualNameProperty()
	{
		return actualName;
	}

	public final boolean getPreferActualName()
	{
		return preferActualName.get();
	}

	public final void setPreferActualName(final boolean preferActualName)
	{
		this.preferActualName.set(preferActualName);
	}

	public final Property<Boolean> preferActualNameProperty()
	{
		return preferActualName;
	}
}

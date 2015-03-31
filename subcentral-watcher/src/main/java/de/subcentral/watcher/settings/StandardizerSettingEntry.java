package de.subcentral.watcher.settings;

import java.util.Objects;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import de.subcentral.core.standardizing.Standardizer;

public abstract class StandardizerSettingEntry<T, S extends Standardizer<? super T>> implements SettingEntry<S>
{
	protected final Class<T>		beanType;
	protected final S				standardizer;
	protected final BooleanProperty	enabled;

	public StandardizerSettingEntry(Class<T> beanType, S standardizer, boolean enabled)
	{
		this.beanType = Objects.requireNonNull(beanType, "beanType");
		this.standardizer = Objects.requireNonNull(standardizer, "standardizer");
		this.enabled = new SimpleBooleanProperty(this, "enabled", enabled);
	}

	public Class<T> getBeanType()
	{
		return beanType;
	}

	@Override
	public S getValue()
	{
		return standardizer;
	}

	public abstract StringBinding standardizerTypeAsStringBinding();

	public abstract StringBinding ruleAsStringBinding();

	@Override
	public final BooleanProperty enabledProperty()
	{
		return this.enabled;
	}

	@Override
	public final boolean isEnabled()
	{
		return this.enabledProperty().get();
	}

	@Override
	public final void setEnabled(final boolean enabled)
	{
		this.enabledProperty().set(enabled);
	}
}
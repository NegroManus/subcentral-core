package de.subcentral.watcher.settings;

import java.util.Objects;

import javafx.beans.binding.StringBinding;
import de.subcentral.core.standardizing.Standardizer;

public abstract class StandardizerSettingEntry<T, S extends Standardizer<? super T>> extends AbstractSettingEntry<S>
{
	protected final Class<T>	beanType;

	public StandardizerSettingEntry(Class<T> beanType, S standardizer, boolean enabled)
	{
		super(standardizer, enabled);
		this.beanType = Objects.requireNonNull(beanType, "beanType");
	}

	public Class<T> getBeanType()
	{
		return beanType;
	}

	public abstract StringBinding standardizerTypeAsStringBinding();

	public abstract StringBinding ruleAsStringBinding();
}
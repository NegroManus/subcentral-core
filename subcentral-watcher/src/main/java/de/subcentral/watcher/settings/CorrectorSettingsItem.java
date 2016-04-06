package de.subcentral.watcher.settings;

import java.util.Objects;

import de.subcentral.core.correct.Corrector;
import de.subcentral.fx.settings.SimpleSettingsItem;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public abstract class CorrectorSettingsItem<T, C extends Corrector<? super T>> extends SimpleSettingsItem<C>
{
	protected final Class<T>		beanType;
	protected final BooleanProperty	beforeQuerying;
	protected final BooleanProperty	afterQuerying;

	public CorrectorSettingsItem(Class<T> beanType, C corrector, boolean beforeQuerying, boolean afterQuerying)
	{
		super(corrector);
		this.beanType = Objects.requireNonNull(beanType, "beanType");
		this.beforeQuerying = new SimpleBooleanProperty(this, "beforeQuerying", beforeQuerying);
		this.afterQuerying = new SimpleBooleanProperty(this, "afterQuerying", afterQuerying);
	}

	public Class<T> getBeanType()
	{
		return beanType;
	}

	public abstract StringBinding ruleTypeBinding();

	public abstract StringBinding ruleBinding();

	public final BooleanProperty beforeQueryingProperty()
	{
		return this.beforeQuerying;
	}

	public final boolean isBeforeQuerying()
	{
		return this.beforeQueryingProperty().get();
	}

	public final void setBeforeQuerying(final boolean beforeQuerying)
	{
		this.beforeQueryingProperty().set(beforeQuerying);
	}

	public final BooleanProperty afterQueryingProperty()
	{
		return this.afterQuerying;
	}

	public final boolean isAfterQuerying()
	{
		return this.afterQueryingProperty().get();
	}

	public final void setAfterQuerying(final boolean afterQuerying)
	{
		this.afterQueryingProperty().set(afterQuerying);
	}

}
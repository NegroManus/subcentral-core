package de.subcentral.fx.dialog;

import javafx.stage.Window;

public abstract class BeanEditController<T> extends DialogController<T>
{
	// Model
	protected final T bean;

	public BeanEditController(T bean, Window owner)
	{
		super(owner);
		this.bean = bean;
	}
}
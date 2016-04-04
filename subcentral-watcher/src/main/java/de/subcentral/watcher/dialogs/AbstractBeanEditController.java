package de.subcentral.watcher.dialogs;

import javafx.stage.Window;

public abstract class AbstractBeanEditController<T> extends AbstractDialogController<T>
{
	// Model
	protected final T bean;

	public AbstractBeanEditController(T bean, Window owner)
	{
		super(owner);
		this.bean = bean;
	}
}
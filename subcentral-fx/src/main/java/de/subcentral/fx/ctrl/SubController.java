package de.subcentral.fx.ctrl;

import java.util.Objects;

import javafx.stage.Stage;

public abstract class SubController<C extends ExecutingController> extends ExecutingController
{
	protected final C parent;

	protected SubController(C parentController)
	{
		this.parent = Objects.requireNonNull(parentController, "parent");
	}

	public C getParent()
	{
		return parent;
	}

	@Override
	public Stage getPrimaryStage()
	{
		return parent.getPrimaryStage();
	}

	@Override
	public TaskExecutor getExecutor()
	{
		return parent.getExecutor();
	}
}

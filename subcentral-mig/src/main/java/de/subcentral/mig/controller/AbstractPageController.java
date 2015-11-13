package de.subcentral.mig.controller;

import javafx.beans.binding.BooleanBinding;
import javafx.scene.Node;

public abstract class AbstractPageController extends AbstractController
{
	protected MainController	mainController;
	protected MigrationConfig	config;

	public AbstractPageController(MainController mainController, MigrationConfig config)
	{
		this.mainController = mainController;
		this.config = config;
	}

	public MainController getMainController()
	{
		return mainController;
	}

	public MigrationConfig getConfig()
	{
		return config;
	}

	public abstract void onEntering();

	public abstract void onExiting();

	public abstract Node getRootPane();

	public abstract BooleanBinding nextButtonDisableBinding();

	@Override
	public void shutdown() throws Exception
	{
		// TODO Auto-generated method stub

	}

}

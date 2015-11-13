package de.subcentral.mig.controller;

import de.subcentral.fx.FxUtil;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

public class MigrationPageController extends AbstractPageController
{
	// View
	@FXML
	private AnchorPane	rootPane;
	@FXML
	private GridPane	contentPane;

	public MigrationPageController(MainController mainController, MigrationConfig config)
	{
		super(mainController, config);
	}

	@Override
	public void initialize() throws Exception
	{
		// TODO Auto-generated method stub

	}

	@Override
	public String getTitle()
	{
		return "Migration";
	}

	@Override
	public Pane getRootPane()
	{
		return rootPane;
	}

	@Override
	public Pane getContentPane()
	{
		return contentPane;
	}

	@Override
	public void onEntering()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onExiting()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public BooleanBinding nextButtonDisableBinding()
	{
		return FxUtil.constantBooleanBinding(true);
	}

	@Override
	public void shutdown() throws Exception
	{
		// TODO Auto-generated method stub

	}

}

package de.subcentral.mig.controller;

import java.net.URL;

import javafx.fxml.FXML;

public abstract class AbstractController
{
	// location is automatically injected before initialize()
	@FXML
	protected URL location;

	/**
	 * Initializes the controller class. This method is automatically called after the fxml file has been loaded.
	 */
	@FXML
	public abstract void initialize() throws Exception;

	public abstract void shutdown() throws Exception;
}

package de.subcentral.fx.ctrl;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.stage.Stage;

public abstract class Controller {
	// location and resources are automatically injected before initialize()
	@FXML
	protected URL				location;
	@FXML
	protected ResourceBundle	resources;

	public abstract Stage getPrimaryStage();

	/**
	 * Initializes the controller class. This method is automatically called after the fxml file has been loaded. Initialize the view components and connect them to the model here.
	 */
	@FXML
	protected abstract void initialize() throws Exception;

	public void shutdown() throws Exception {

	}
}

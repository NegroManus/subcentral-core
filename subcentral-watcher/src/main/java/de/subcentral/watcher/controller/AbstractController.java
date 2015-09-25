package de.subcentral.watcher.controller;

import java.net.URL;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.fxml.FXML;

public abstract class AbstractController
{
	private static final Logger log = LogManager.getLogger(AbstractController.class);

	// location and resources are automatically injected before initialize()
	@FXML
	protected URL				location;
	@FXML
	protected ResourceBundle	resources;

	/**
	 * Initializes the controller class. This method is automatically called after the fxml file has been loaded.
	 */
	@FXML
	public void initialize() throws Exception
	{
		log.debug("Initializing {} (location={}, resources={}) ...", getClass().getSimpleName(), location, resources == null ? null : resources.getBaseBundleName());
		try
		{
			long start = System.currentTimeMillis();
			doInitialize();
			log.debug("Initialized {} in {} ms", getClass().getSimpleName(), (System.currentTimeMillis() - start));
		}
		catch (Exception e)
		{
			log.error("Initialization of " + getClass().getSimpleName() + " failed", e);
			throw e;
		}
	}

	/**
	 * Initialize the view components and connect them to the model here.
	 * 
	 * @throws Exception
	 */
	protected abstract void doInitialize() throws Exception;

	public void shutdown() throws Exception
	{

	}
}

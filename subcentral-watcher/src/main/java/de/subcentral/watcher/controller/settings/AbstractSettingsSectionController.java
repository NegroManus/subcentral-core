package de.subcentral.watcher.controller.settings;

import javafx.scene.Node;
import de.subcentral.watcher.controller.AbstractController;

public abstract class AbstractSettingsSectionController extends AbstractController
{
	protected final SettingsController settingsController;

	public AbstractSettingsSectionController(SettingsController settingsController)
	{
		this.settingsController = settingsController;
	}

	public SettingsController getSettingsController()
	{
		return settingsController;
	}

	public abstract Node getContentPane();

}

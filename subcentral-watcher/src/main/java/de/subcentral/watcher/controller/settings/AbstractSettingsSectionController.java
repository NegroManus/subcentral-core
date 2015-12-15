package de.subcentral.watcher.controller.settings;

import de.subcentral.fx.Controller;
import javafx.scene.Node;

public abstract class AbstractSettingsSectionController extends Controller
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

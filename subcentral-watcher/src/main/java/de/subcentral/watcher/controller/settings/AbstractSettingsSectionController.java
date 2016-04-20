package de.subcentral.watcher.controller.settings;

import de.subcentral.fx.SubController;
import javafx.scene.Node;

public abstract class AbstractSettingsSectionController extends SubController<SettingsController>
{
	public AbstractSettingsSectionController(SettingsController settingsController)
	{
		super(settingsController);
	}

	public abstract Node getContentPane();
}

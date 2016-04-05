package de.subcentral.watcher.dialogs;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.subcentral.core.metadata.release.CrossGroupCompatibility;
import de.subcentral.core.metadata.release.StandardRelease;
import de.subcentral.fx.FxUtil;
import de.subcentral.fx.dialog.DialogController;
import de.subcentral.watcher.dialogs.ImportSettingEntriesController.ImportSettingEntriesParameters;
import de.subcentral.watcher.settings.LanguageToTextMapping;
import de.subcentral.watcher.settings.PatternToLanguageMapping;
import de.subcentral.watcher.settings.ReleaseTagsCorrectionRuleSettingsItem;
import de.subcentral.watcher.settings.SeriesNameCorrectionRuleSettingsItem;
import javafx.stage.Window;

public class WatcherDialogs
{
	private static final Logger log = LogManager.getLogger(WatcherDialogs.class);

	public static Optional<StandardRelease> showStandardReleaseEditView(Window window)
	{
		return showStandardReleaseEditView(null, window);
	}

	public static Optional<StandardRelease> showStandardReleaseEditView(StandardRelease standardRls, Window window)
	{
		StandardReleaseEditController ctrl = new StandardReleaseEditController(standardRls, window);
		return showEditViewAndWait(ctrl, "StandardReleaseEditView.fxml");
	}

	public static Optional<CrossGroupCompatibility> showCrossGroupCompatibilityEditView(Window window)
	{
		return showCrossGroupCompatibilityEditView(null, window);
	}

	public static Optional<CrossGroupCompatibility> showCrossGroupCompatibilityEditView(CrossGroupCompatibility crossGroupCompatibility, Window window)
	{
		CrossGroupCompatibilityEditController ctrl = new CrossGroupCompatibilityEditController(crossGroupCompatibility, window);
		return showEditViewAndWait(ctrl, "CrossGroupCompatibilityEditView.fxml");
	}

	public static Optional<SeriesNameCorrectionRuleSettingsItem> showSeriesNameCorrectionRuleEditView(Window window)
	{
		return showSeriesNameCorrectionRuleEditView(null, window);
	}

	public static Optional<SeriesNameCorrectionRuleSettingsItem> showSeriesNameCorrectionRuleEditView(SeriesNameCorrectionRuleSettingsItem entry, Window window)
	{
		SeriesNameCorrectionRuleEditController ctrl = new SeriesNameCorrectionRuleEditController(entry, window);
		return showEditViewAndWait(ctrl, "SeriesNameCorrectionRuleEditView.fxml");
	}

	public static Optional<ReleaseTagsCorrectionRuleSettingsItem> showReleaseTagsCorrectionRuleEditView(Window window)
	{
		return showReleaseTagsCorrectionRuleEditView(null, window);
	}

	public static Optional<ReleaseTagsCorrectionRuleSettingsItem> showReleaseTagsCorrectionRuleEditView(ReleaseTagsCorrectionRuleSettingsItem entry, Window window)
	{
		ReleaseTagsCorrectionRuleEditController ctrl = new ReleaseTagsCorrectionRuleEditController(entry, window);
		return showEditViewAndWait(ctrl, "ReleaseTagsCorrectionRuleEditView.fxml");
	}

	public static Optional<List<Locale>> showLocaleListEditView(List<Locale> languages, Window window)
	{
		LocaleListEditController ctrl = new LocaleListEditController(languages, window);
		return showEditViewAndWait(ctrl, "LocaleListEditView.fxml");
	}

	public static Optional<PatternToLanguageMapping> showTextLanguageMappingEditView(Window window)
	{
		return showTextLanguageMappingEditView(null, window);
	}

	public static Optional<PatternToLanguageMapping> showTextLanguageMappingEditView(PatternToLanguageMapping mapping, Window window)
	{
		TextLanguageMappingEditController ctrl = new TextLanguageMappingEditController(mapping, window);
		return showEditViewAndWait(ctrl, "TextLanguageMappingEditView.fxml");
	}

	public static Optional<LanguageToTextMapping> showLanguageTextMappingEditView(Window window)
	{
		return showLanguageTextMappingEditView(null, window);
	}

	public static Optional<LanguageToTextMapping> showLanguageTextMappingEditView(LanguageToTextMapping mapping, Window window)
	{
		LanguageTextMappingEditController ctrl = new LanguageTextMappingEditController(mapping, window);
		return showEditViewAndWait(ctrl, "LanguageTextMappingEditView.fxml");
	}

	public static Optional<ImportSettingEntriesParameters> showImportSettingEntriesView(Window window)
	{
		ImportSettingEntriesController ctrl = new ImportSettingEntriesController(window);
		return showEditViewAndWait(ctrl, "ImportSettingEntriesView.fxml");
	}

	private static <T> Optional<T> showEditViewAndWait(DialogController<T> ctrl, String fxmlFilename)
	{
		try
		{
			FxUtil.loadFromFxml(fxmlFilename, null, null, ctrl);
		}
		catch (IOException e)
		{
			log.error("Error while loading FXML " + fxmlFilename + " with controller " + ctrl, e);
			return Optional.empty();
		}
		return ctrl.getDialog().showAndWait();
	}

	private WatcherDialogs()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}

}

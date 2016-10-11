package de.subcentral.watcher.dialog;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.subcentral.core.metadata.release.CrossGroupCompatibilityRule;
import de.subcentral.core.metadata.release.StandardRelease;
import de.subcentral.fx.FxIO;
import de.subcentral.fx.dialog.DialogController;
import de.subcentral.watcher.dialog.ImportSettingItemsController.ImportSettingItemsParameters;
import de.subcentral.watcher.settings.LanguageToTextMapping;
import de.subcentral.watcher.settings.PatternToLanguageMapping;
import de.subcentral.watcher.settings.ReleaseTagsCorrectorSettingsItem;
import de.subcentral.watcher.settings.SeriesNameCorrectorSettingsItem;
import javafx.stage.Window;

public class WatcherDialogs {
    private static final Logger log = LogManager.getLogger(WatcherDialogs.class);

    public static Optional<StandardRelease> showStandardReleaseEditView(Window window) {
        return showStandardReleaseEditView(null, window);
    }

    public static Optional<StandardRelease> showStandardReleaseEditView(StandardRelease standardRls, Window window) {
        StandardReleaseEditController ctrl = new StandardReleaseEditController(standardRls, window);
        return showEditViewAndWait(ctrl, "StandardReleaseEditView.fxml");
    }

    public static Optional<CrossGroupCompatibilityRule> showCrossGroupCompatibilityRuleEditView(Window window) {
        return showCrossGroupCompatibilityRuleEditView(null, window);
    }

    public static Optional<CrossGroupCompatibilityRule> showCrossGroupCompatibilityRuleEditView(CrossGroupCompatibilityRule crossGroupCompatibilityRule, Window window) {
        CrossGroupCompatibilityRuleEditController ctrl = new CrossGroupCompatibilityRuleEditController(crossGroupCompatibilityRule, window);
        return showEditViewAndWait(ctrl, "CrossGroupCompatibilityRuleEditView.fxml");
    }

    public static Optional<SeriesNameCorrectorSettingsItem> showSeriesNameCorrectionRuleEditView(Window window) {
        return showSeriesNameCorrectionRuleEditView(null, window);
    }

    public static Optional<SeriesNameCorrectorSettingsItem> showSeriesNameCorrectionRuleEditView(SeriesNameCorrectorSettingsItem entry, Window window) {
        SeriesNameCorrectionRuleEditController ctrl = new SeriesNameCorrectionRuleEditController(entry, window);
        return showEditViewAndWait(ctrl, "SeriesNameCorrectionRuleEditView.fxml");
    }

    public static Optional<ReleaseTagsCorrectorSettingsItem> showReleaseTagsCorrectionRuleEditView(Window window) {
        return showReleaseTagsCorrectionRuleEditView(null, window);
    }

    public static Optional<ReleaseTagsCorrectorSettingsItem> showReleaseTagsCorrectionRuleEditView(ReleaseTagsCorrectorSettingsItem entry, Window window) {
        ReleaseTagsCorrectionRuleEditController ctrl = new ReleaseTagsCorrectionRuleEditController(entry, window);
        return showEditViewAndWait(ctrl, "ReleaseTagsCorrectionRuleEditView.fxml");
    }

    public static Optional<List<Locale>> showLocaleListEditView(List<Locale> languages, Window window) {
        LocaleListEditController ctrl = new LocaleListEditController(languages, window);
        return showEditViewAndWait(ctrl, "LocaleListEditView.fxml");
    }

    public static Optional<PatternToLanguageMapping> showTextLanguageMappingEditView(Window window) {
        return showTextLanguageMappingEditView(null, window);
    }

    public static Optional<PatternToLanguageMapping> showTextLanguageMappingEditView(PatternToLanguageMapping mapping, Window window) {
        TextLanguageMappingEditController ctrl = new TextLanguageMappingEditController(mapping, window);
        return showEditViewAndWait(ctrl, "TextLanguageMappingEditView.fxml");
    }

    public static Optional<LanguageToTextMapping> showLanguageTextMappingEditView(Window window) {
        return showLanguageTextMappingEditView(null, window);
    }

    public static Optional<LanguageToTextMapping> showLanguageTextMappingEditView(LanguageToTextMapping mapping, Window window) {
        LanguageTextMappingEditController ctrl = new LanguageTextMappingEditController(mapping, window);
        return showEditViewAndWait(ctrl, "LanguageTextMappingEditView.fxml");
    }

    public static Optional<ImportSettingItemsParameters> showImportSettingItemsView(Window window, String title) {
        ImportSettingItemsController ctrl = new ImportSettingItemsController(window, title);
        return showEditViewAndWait(ctrl, "ImportSettingItemsView.fxml");
    }

    private static <T> Optional<T> showEditViewAndWait(DialogController<T> ctrl, String fxmlFilename) {
        try {
            FxIO.loadView(fxmlFilename, ctrl);
        }
        catch (IOException e) {
            log.error("Error while loading FXML " + fxmlFilename + " with controller " + ctrl, e);
            return Optional.empty();
        }
        return ctrl.getDialog().showAndWait();
    }

    private WatcherDialogs() {
        throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
    }

}

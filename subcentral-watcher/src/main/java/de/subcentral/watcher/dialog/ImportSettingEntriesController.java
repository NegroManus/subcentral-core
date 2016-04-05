package de.subcentral.watcher.dialog;

import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;

import de.subcentral.fx.FxUtil;
import de.subcentral.fx.dialog.DialogController;
import de.subcentral.watcher.dialog.ImportSettingEntriesController.ImportSettingEntriesParameters;
import de.subcentral.watcher.dialog.ImportSettingEntriesController.ImportSettingEntriesParameters.SourceType;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;

public class ImportSettingEntriesController extends DialogController<ImportSettingEntriesParameters>
{
	@FXML
	private RadioButton	defaultSettingsRadioBtn;
	@FXML
	private RadioButton	fileRadioBtn;
	@FXML
	private TextField	fileTxtFld;
	@FXML
	private Button		chooseFileBtn;
	@FXML
	private RadioButton	urlRadioBtn;
	@FXML
	private TextField	urlTxtFld;

	@FXML
	private CheckBox	addEntriesCheckBox;
	@FXML
	private CheckBox	replaceEntriesCheckBox;
	@FXML
	private CheckBox	removeEntriesCheckBox;

	public ImportSettingEntriesController(Window window)
	{
		super(window);
	}

	@Override
	protected String getTitle()
	{
		return "Import setting entries";
	}

	@Override
	protected String getImagePath()
	{
		return "upload_16.png";
	}

	@Override
	protected Node getDefaultFocusNode()
	{
		return defaultSettingsRadioBtn;
	}

	@Override
	protected void initComponents()
	{
		// Init
		ToggleGroup sourceTypeToggleGrp = new ToggleGroup();
		sourceTypeToggleGrp.getToggles().addAll(defaultSettingsRadioBtn, fileRadioBtn, urlRadioBtn);
		sourceTypeToggleGrp.selectToggle(defaultSettingsRadioBtn);

		addEntriesCheckBox.setSelected(true);

		// Bindings
		TextFormatter<Path> fileFormatter = FxUtil.bindTextFieldToPath(fileTxtFld);
		ExtensionFilter xmlExtFilter = new ExtensionFilter("XML file", "*.xml");
		FxUtil.setChooseFileAction(chooseFileBtn, fileFormatter, dialog.getDialogPane().getScene().getWindow(), "Choose settings file", xmlExtFilter);
		TextFormatter<URL> urlFormatter = FxUtil.bindTextFieldToUrl(urlTxtFld);

		// Bind apply button
		Node applyButton = dialog.getDialogPane().lookupButton(ButtonType.APPLY);
		applyButton.disableProperty().bind(new BooleanBinding()
		{
			{
				super.bind(sourceTypeToggleGrp.selectedToggleProperty(), fileFormatter.valueProperty(), urlFormatter.valueProperty());
			}

			@Override
			protected boolean computeValue()
			{
				Toggle sourceTypeToggle = sourceTypeToggleGrp.getSelectedToggle();
				if (defaultSettingsRadioBtn == sourceTypeToggle)
				{
					return false;
				}
				if (fileRadioBtn == sourceTypeToggle)
				{
					return fileFormatter.getValue() == null;
				}
				if (urlRadioBtn == sourceTypeToggle)
				{
					return urlFormatter.getValue() == null;
				}
				return true;
			}
		});

		// Set ResultConverter
		dialog.setResultConverter(dialogButton ->
		{
			if (dialogButton == ButtonType.APPLY)
			{
				Toggle sourceTypeToggle = sourceTypeToggleGrp.getSelectedToggle();
				SourceType sourceType;
				if (defaultSettingsRadioBtn == sourceTypeToggle)
				{
					sourceType = SourceType.DEFAULT_SETTINGS;
				}
				else if (fileRadioBtn == sourceTypeToggle)
				{
					sourceType = SourceType.FILE;
				}
				else if (urlRadioBtn == sourceTypeToggle)
				{
					sourceType = SourceType.URL;
				}
				else
				{
					sourceType = SourceType.DEFAULT_SETTINGS;
				}
				Path file = fileFormatter.getValue();
				URL url = urlFormatter.getValue();
				boolean addEntries = addEntriesCheckBox.isSelected();
				boolean replaceEntries = replaceEntriesCheckBox.isSelected();
				boolean removeEntries = removeEntriesCheckBox.isSelected();
				return new ImportSettingEntriesParameters(sourceType, file, url, addEntries, replaceEntries, removeEntries);
			}
			return null;
		});
	}

	public static class ImportSettingEntriesParameters
	{
		public enum SourceType
		{
			FILE, URL, DEFAULT_SETTINGS
		}

		private final SourceType	sourceType;
		private final Path			file;
		private final URL			url;
		private final boolean		addEntries;
		private final boolean		replaceEntries;
		private final boolean		removeEntries;

		public ImportSettingEntriesParameters(SourceType sourceType, Path file, URL url, boolean addEntries, boolean replaceEntries, boolean removeEntries)
		{
			this.sourceType = Objects.requireNonNull(sourceType, "sourceType");
			if (SourceType.FILE == sourceType)
			{
				Objects.requireNonNull(file, "file");
			}
			this.file = file;
			if (SourceType.URL == sourceType)
			{
				Objects.requireNonNull(url, "url");
			}
			this.url = url;
			this.addEntries = addEntries;
			this.replaceEntries = replaceEntries;
			this.removeEntries = removeEntries;
		}

		public SourceType getSourceType()
		{
			return sourceType;
		}

		public Path getFile()
		{
			return file;
		}

		public URL getUrl()
		{
			return url;
		}

		public boolean isAddEntries()
		{
			return addEntries;
		}

		public boolean isReplaceEntries()
		{
			return replaceEntries;
		}

		public boolean isRemoveEntries()
		{
			return removeEntries;
		}
	}
}
package de.subcentral.watcher.dialog;

import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;

import de.subcentral.fx.FxActions;
import de.subcentral.fx.FxControlBindings;
import de.subcentral.fx.dialog.DialogController;
import de.subcentral.watcher.dialog.ImportSettingItemsController.ImportSettingItemsParameters;
import de.subcentral.watcher.dialog.ImportSettingItemsController.ImportSettingItemsParameters.SourceType;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
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

public class ImportSettingItemsController extends DialogController<ImportSettingItemsParameters>
{
	private String		title;
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
	private CheckBox	addItemsCheckBox;
	@FXML
	private CheckBox	replaceItemsCheckBox;
	@FXML
	private CheckBox	removeItemsCheckBox;

	public ImportSettingItemsController(Window window, String title)
	{
		super(window);
		this.title = (title != null ? title : "Import settings items");
	}

	@Override
	protected String getTitle()
	{
		return title;
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

		addItemsCheckBox.setSelected(true);

		// Bindings
		TextFormatter<Path> fileFormatter = FxControlBindings.bindTextFieldToPath(fileTxtFld);
		ExtensionFilter xmlExtFilter = new ExtensionFilter("XML file", "*.xml");
		chooseFileBtn.setOnAction((ActionEvent evt) -> FxActions.chooseFile(fileFormatter, dialog.getDialogPane().getScene().getWindow(), "Choose settings file", xmlExtFilter));
		TextFormatter<URL> urlFormatter = FxControlBindings.bindTextFieldToUrl(urlTxtFld);

		// Bind apply button
		Node applyButton = dialog.getDialogPane().lookupButton(ButtonType.APPLY);
		applyButton.disableProperty().bind(new BooleanBinding()
		{
			{
				super.bind(sourceTypeToggleGrp.selectedToggleProperty(),
						fileFormatter.valueProperty(),
						urlFormatter.valueProperty(),
						addItemsCheckBox.selectedProperty(),
						replaceItemsCheckBox.selectedProperty(),
						removeItemsCheckBox.selectedProperty());
			}

			@Override
			protected boolean computeValue()
			{
				Toggle sourceTypeToggle = sourceTypeToggleGrp.getSelectedToggle();
				return (fileRadioBtn == sourceTypeToggle && fileFormatter.getValue() == null || urlRadioBtn == sourceTypeToggle && urlFormatter.getValue() == null)
						|| (!addItemsCheckBox.isSelected() && !replaceItemsCheckBox.isSelected() && !removeItemsCheckBox.isSelected());
			}
		});

		// Set ResultConverter
		dialog.setResultConverter(dialogButton ->
		{
			if (dialogButton == ButtonType.APPLY)
			{
				Toggle sourceTypeToggle = sourceTypeToggleGrp.getSelectedToggle();
				SourceType sourceType;
				if (fileRadioBtn == sourceTypeToggle)
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
				boolean addEntries = addItemsCheckBox.isSelected();
				boolean replaceEntries = replaceItemsCheckBox.isSelected();
				boolean removeEntries = removeItemsCheckBox.isSelected();
				return new ImportSettingItemsParameters(sourceType, file, url, addEntries, replaceEntries, removeEntries);
			}
			return null;
		});
	}

	public static class ImportSettingItemsParameters
	{
		public enum SourceType
		{
			FILE, URL, DEFAULT_SETTINGS
		}

		private final SourceType	sourceType;
		private final Path			file;
		private final URL			url;
		private final boolean		addItems;
		private final boolean		replaceItems;
		private final boolean		removeItems;

		public ImportSettingItemsParameters(SourceType sourceType, Path file, URL url, boolean addItems, boolean replaceItems, boolean removeItems)
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
			this.addItems = addItems;
			this.replaceItems = replaceItems;
			this.removeItems = removeItems;
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

		public boolean isAddItems()
		{
			return addItems;
		}

		public boolean isReplaceItems()
		{
			return replaceItems;
		}

		public boolean isRemoveItems()
		{
			return removeItems;
		}
	}
}
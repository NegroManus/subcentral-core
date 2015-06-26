package de.subcentral.watcher.controller.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.subcentral.watcher.settings.ProcessingSettings;
import de.subcentral.watcher.settings.WatcherSettings;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.GridPane;

public class NamingSettingsController extends AbstractSettingsSectionController
{
    @FXML
    private GridPane			      namingSettingsPane;
    @FXML
    private TableView<NamingParam>	      namingParamsTableView;
    @FXML
    private TableColumn<NamingParam, String>  namingParamsNameColumn;
    @FXML
    private TableColumn<NamingParam, Boolean> namingParamsValueColumn;

    public NamingSettingsController(SettingsController settingsController)
    {
	super(settingsController);
    }

    @Override
    public GridPane getSectionRootPane()
    {
	return namingSettingsPane;
    }

    @Override
    protected void doInitialize() throws Exception
    {
	final ProcessingSettings settings = WatcherSettings.INSTANCE.getProcessingSettings();

	// Naming parameters
	List<NamingParam> namingParams = new ArrayList<>();
	for (Map.Entry<String, Object> entries : settings.getNamingParameters().entrySet())
	{
	    namingParams.add(new NamingParam(entries.getKey(), (Boolean) entries.getValue()));
	}

	final InvalidationListener listener = (Observable observable) -> {
	    NamingParam namingParam = (NamingParam) ((BooleanProperty) observable).getBean();
	    settings.getNamingParameters().put(namingParam.getKey(), namingParam.getValue());
	};
	for (NamingParam namingParam : namingParams)
	{
	    namingParam.valueProperty().addListener(listener);
	}

	namingParamsTableView.getItems().addAll(namingParams);

	namingParamsNameColumn.setCellValueFactory((CellDataFeatures<NamingParam, String> param) -> param.getValue().keyProperty());
	namingParamsValueColumn.setCellValueFactory((CellDataFeatures<NamingParam, Boolean> param) -> param.getValue().valueProperty());
	namingParamsValueColumn.setCellFactory(CheckBoxTableCell.forTableColumn(namingParamsValueColumn));
    }

    public static class NamingParam
    {
	private final ReadOnlyStringWrapper key;
	private final BooleanProperty	    value;

	private NamingParam(String key, boolean value)
	{
	    this.key = new ReadOnlyStringWrapper(this, "key", key);
	    this.value = new SimpleBooleanProperty(this, "value", value);
	}

	public String getKey()
	{
	    return key.get();
	}

	public ReadOnlyStringProperty keyProperty()
	{
	    return key.getReadOnlyProperty();
	}

	public boolean getValue()
	{
	    return value.get();
	}

	public void setValue(boolean value)
	{
	    this.value.set(value);
	}

	public BooleanProperty valueProperty()
	{
	    return value;
	}
    }
}

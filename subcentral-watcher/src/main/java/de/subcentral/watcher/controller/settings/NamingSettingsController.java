package de.subcentral.watcher.controller.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.subcentral.watcher.settings.ProcessingSettings;
import de.subcentral.watcher.settings.WatcherSettings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
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
	// bind table items to settings
	new NamingParamBinding(namingParamsTableView.getItems(), settings.namingParametersProperty());

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

    private class NamingParamBinding
    {
	private final ObservableList<NamingParam> list;
	private final MapProperty<String, Object> map;
	final ChangeListener<Boolean>		  listItemValueListener;
	final MapChangeListener<String, Object>	  mapListener;
	private boolean				  updating;

	private NamingParamBinding(ObservableList<NamingParam> list, MapProperty<String, Object> map)
	{
	    this.list = list;
	    this.map = map;

	    // set initial value
	    List<NamingParam> namingParams = new ArrayList<>();
	    for (Map.Entry<String, Object> entries : map.entrySet())
	    {
		namingParams.add(new NamingParam(entries.getKey(), (Boolean) entries.getValue()));
	    }
	    list.setAll(namingParams);

	    // init listeners and add them
	    listItemValueListener = initListItemValueListener();
	    mapListener = initMapListener();
	    for (NamingParam param : list)
	    {
		param.valueProperty().addListener(listItemValueListener);
	    }
	    map.addListener(mapListener);
	}

	private void unbind()
	{
	    for (NamingParam param : list)
	    {
		param.valueProperty().removeListener(listItemValueListener);
	    }
	    map.removeListener(mapListener);
	}

	private ChangeListener<Boolean> initListItemValueListener()
	{
	    return new ChangeListener<Boolean>()
	    {
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
		{
		    if (!updating)
		    {
			updating = true;
			try
			{
			    NamingParam param = (NamingParam) ((BooleanProperty) observable).getBean();
			    map.put(param.getKey(), param.getValue());
			}
			finally
			{
			    updating = false;
			}
		    }
		}
	    };
	}

	private MapChangeListener<String, Object> initMapListener()
	{
	    return new MapChangeListener<String, Object>()
	    {
		@Override
		public void onChanged(MapChangeListener.Change<? extends String, ? extends Object> change)
		{
		    if (!updating)
		    {
			updating = true;
			try
			{
			    if (change.wasAdded())
			    {
				for (NamingParam param : list)
				{
				    if (change.getKey().equals(param.getKey()))
				    {
					param.setValue((Boolean) change.getValueAdded());
				    }
				}
			    }
			}
			finally
			{
			    updating = false;
			}
		    }
		}
	    };
	}
    }
}

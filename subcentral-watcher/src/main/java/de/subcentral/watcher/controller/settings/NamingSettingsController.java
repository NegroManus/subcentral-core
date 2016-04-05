package de.subcentral.watcher.controller.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.subcentral.core.name.EpisodeNamer;
import de.subcentral.core.name.ReleaseNamer;
import de.subcentral.core.name.SubtitleNamer;
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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.GridPane;

public class NamingSettingsController extends AbstractSettingsSectionController
{
	@FXML
	private GridPane							rootPane;
	@FXML
	private TableView<NamingParam>				namingParamsTableView;
	@FXML
	private TableColumn<NamingParam, String>	namingParamsKeyColumn;
	@FXML
	private TableColumn<NamingParam, Boolean>	namingParamsValueColumn;

	public NamingSettingsController(SettingsController settingsController)
	{
		super(settingsController);
	}

	@Override
	public GridPane getContentPane()
	{
		return rootPane;
	}

	@Override
	protected void initialize() throws Exception
	{
		final ProcessingSettings settings = WatcherSettings.INSTANCE.getProcessingSettings();

		// Naming parameters
		// bind table items to settings
		new NamingParamBinding(namingParamsTableView.getItems(), settings.namingParametersProperty());

		namingParamsKeyColumn.setCellValueFactory((CellDataFeatures<NamingParam, String> param) -> param.getValue().keyProperty());
		namingParamsKeyColumn.setCellFactory((TableColumn<NamingParam, String> column) -> new KeyTableCell());
		namingParamsValueColumn.setCellValueFactory((CellDataFeatures<NamingParam, Boolean> param) -> param.getValue().valueProperty());
		namingParamsValueColumn.setCellFactory(CheckBoxTableCell.forTableColumn(namingParamsValueColumn));
	}

	private static class NamingParam
	{
		private final ReadOnlyStringWrapper	key;
		private final BooleanProperty		value;

		private NamingParam(String key, boolean value)
		{
			this.key = new ReadOnlyStringWrapper(this, "key", key);
			this.value = new SimpleBooleanProperty(this, "item", value);
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

	private static class NamingParamBinding
	{
		private final ObservableList<NamingParam>		list;
		private final MapProperty<String, Object>		map;
		private final ChangeListener<Boolean>			listItemValueListener;
		private final MapChangeListener<String, Object>	mapListener;
		private boolean									updating;

		private NamingParamBinding(ObservableList<NamingParam> list, MapProperty<String, Object> map)
		{
			this.list = Objects.requireNonNull(list, "list");
			this.map = Objects.requireNonNull(map, "map");

			// set initial item
			List<NamingParam> namingParams = new ArrayList<>();
			for (Map.Entry<String, Object> entries : map.entrySet())
			{
				namingParams.add(new NamingParam(entries.getKey(), (Boolean) entries.getValue()));
			}
			list.setAll(namingParams);

			// init listeners and add them
			listItemValueListener = createListItemValueListener();
			mapListener = createMapListener();
			for (NamingParam param : list)
			{
				param.valueProperty().addListener(listItemValueListener);
			}
			map.addListener(mapListener);
		}

		// not currently used because NamingParamBinding is initialized once and never removed
		private void unbind()
		{
			for (NamingParam param : list)
			{
				param.valueProperty().removeListener(listItemValueListener);
			}
			map.removeListener(mapListener);
		}

		private ChangeListener<Boolean> createListItemValueListener()
		{
			return (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) ->
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
			};
		}

		private MapChangeListener<String, Object> createMapListener()
		{
			return (MapChangeListener.Change<? extends String, ? extends Object> change) ->
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
			};
		}
	}

	private static class KeyTableCell extends TableCell<NamingParam, String>
	{
		@Override
		public void updateItem(String item, boolean empty)
		{
			super.updateItem(item, empty);
			if (empty || item == null)
			{
				setText(null);
			}
			else
			{
				if (EpisodeNamer.PARAM_ALWAYS_INCLUDE_TITLE.equals(item))
				{
					setText("Episode: Always include episode title");
				}
				else if (ReleaseNamer.PARAM_PREFER_NAME.equals(item))
				{
					setText("Release: Prefer release name over computed name");
				}
				else if (SubtitleNamer.PARAM_INCLUDE_GROUP.equals(item))
				{
					setText("Subtitle: Include group (such as \"SubCentral\")");
				}
				else if (SubtitleNamer.PARAM_INCLUDE_SOURCE.equals(item))
				{
					setText("Subtitle: Include source (such as \"Addic7ed.com\")");
				}
				else
				{
					setText(item);
				}
			}
		}
	}
}

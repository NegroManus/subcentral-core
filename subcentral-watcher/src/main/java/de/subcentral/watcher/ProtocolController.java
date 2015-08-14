package de.subcentral.watcher;

import java.util.Objects;

import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.standardizing.StandardizingChange;
import de.subcentral.fx.FxUtil;
import de.subcentral.watcher.controller.AbstractController;
import de.subcentral.watcher.controller.processing.ProcessingTask;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.Callback;

public class ProtocolController extends AbstractController
{
    // Model
    private final ProcessingTask task;

    // View
    @FXML
    private Label				     sourceFileLabel;
    @FXML
    private TextField				     parsedObjTextField;
    @FXML
    private TableView<StandardizingChange>	     parsingCorrectionsTableView;
    @FXML
    private TableColumn<StandardizingChange, String> parsingCorrectionsAttributeColumn;
    @FXML
    private TableColumn<StandardizingChange, Object> parsingCorrectionsOldValueColumn;
    @FXML
    private TableColumn<StandardizingChange, Object> parsingCorrectionsNewValueColumn;
    @FXML
    private ListView<Release>			     foundReleasesListView;
    @FXML
    private ListView<Release>			     matchingReleasesListView;
    @FXML
    private ListView<Release>			     compatibleReleasesListView;

    public ProtocolController(ProcessingTask task)
    {
	this.task = Objects.requireNonNull(task, "task");
    }

    @Override
    protected void doInitialize() throws Exception
    {
	final Callback<ListView<Release>, ListCell<Release>> rlsCellFactory = (ListView<Release> param) -> {
	    return new ListCell<Release>()
	    {
		@Override
		protected void updateItem(Release rls, boolean empty)
		{
		    super.updateItem(rls, empty);

		    if (empty || rls == null)
		    {
			setText("");
			setGraphic(null);
		    }
		    else
		    {
			setText(task.name(rls));
			setGraphic(WatcherFxUtil.createFurtherInfoHyperlink(rls, task.getController().getMainController().getCommonExecutor()));
		    }
		}
	    };
	};

	sourceFileLabel.setText(task.getSourceFile().getFileName().toString());

	parsedObjTextField.setText(task.name(task.getParsedObject()));

	parsingCorrectionsTableView.setItems(FXCollections.observableList(task.getParsingCorrections()));
	parsingCorrectionsAttributeColumn.setCellValueFactory((TableColumn.CellDataFeatures<StandardizingChange, String> features) -> {
	    StandardizingChange c = features.getValue();
	    return FxUtil.constantStringBinding(WatcherFxUtil.beanTypeToString(c.getBean().getClass()) + ": " + c.getPropertyName());
	});
	parsingCorrectionsOldValueColumn.setCellValueFactory((TableColumn.CellDataFeatures<StandardizingChange, Object> features) -> {
	    return FxUtil.constantBinding(features.getValue().getOldValue());
	});
	parsingCorrectionsNewValueColumn.setCellValueFactory((TableColumn.CellDataFeatures<StandardizingChange, Object> features) -> {
	    return FxUtil.constantBinding(features.getValue().getNewValue());
	});

	foundReleasesListView.setItems(FXCollections.observableList(task.getFoundReleases()));
	foundReleasesListView.setCellFactory(rlsCellFactory);

	matchingReleasesListView.setItems(FXCollections.observableList(task.getMatchingReleases()));
	matchingReleasesListView.setCellFactory(rlsCellFactory);

	compatibleReleasesListView.setItems(FXCollections.observableArrayList(task.getCompatibleReleases().keySet()));
	compatibleReleasesListView.setCellFactory(rlsCellFactory);
    }

}
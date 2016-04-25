package de.subcentral.fx.action;

import java.util.function.Predicate;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.TableView;

public class RemoveAction<E> extends AbstractItemAction<E>
{
	protected Predicate<? super E> removeConfirmer;

	public RemoveAction(ListView<E> listView)
	{
		super(listView.getItems(), listView.getSelectionModel());
	}

	public RemoveAction(TableView<E> tableView)
	{
		super(tableView.getItems(), tableView.getSelectionModel());
	}

	public RemoveAction(ObservableList<E> items, SelectionModel<E> selectionModel)
	{
		super(items, selectionModel);
	}

	public Predicate<? super E> getRemoveConfirmer()
	{
		return removeConfirmer;
	}

	public void setRemoveConfirmer(Predicate<? super E> removeConfirmer)
	{
		this.removeConfirmer = removeConfirmer;
	}

	@Override
	public void handle(ActionEvent event)
	{
		if (selectionModel.getSelectedItem() != null && (removeConfirmer == null || removeConfirmer.test(selectionModel.getSelectedItem())))
		{
			int selectedIndex = selectionModel.getSelectedIndex();
			items.remove(selectedIndex);
			// Reselect the selectedIndex (standard behavior selects selectedIndex-1)
			if (!items.isEmpty() && selectedIndex < items.size())
			{
				selectionModel.select(selectedIndex);
			}
		}
	}
}

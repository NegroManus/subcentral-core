package de.subcentral.fx.action;

import java.util.Objects;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.SelectionModel;

public abstract class AbstractItemAction<E> implements EventHandler<ActionEvent>
{
	protected final ObservableList<E>	items;
	protected final SelectionModel<E>	selectionModel;

	public AbstractItemAction(ObservableList<E> items, SelectionModel<E> selectionModel)
	{
		this.items = Objects.requireNonNull(items, "items");
		this.selectionModel = Objects.requireNonNull(selectionModel, "selectionModel");
	}
}

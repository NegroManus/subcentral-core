package de.subcentral.fx;

import java.util.function.Predicate;

import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

public class FxNodes
{
	private FxNodes()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}

	public static HBox createDefaultHBox()
	{
		HBox hbox = new HBox();
		hbox.setSpacing(5d);
		hbox.setAlignment(Pos.CENTER_LEFT);
		return hbox;
	}

	public static <T> TreeItem<T> findTreeItem(TreeItem<T> treeItem, Predicate<TreeItem<T>> predicate)
	{
		if (predicate.test(treeItem))
		{
			return treeItem;
		}
		for (TreeItem<T> child : treeItem.getChildren())
		{
			TreeItem<T> matchingItem = findTreeItem(child, predicate);
			if (matchingItem != null)
			{
				return matchingItem;
			}
		}
		return null;
	}

	/**
	 * Workaround for http://stackoverflow.com/questions/28937392/javafx-alerts-and-their-size;
	 * 
	 * @param alert
	 *            the alert
	 */
	public static void fixAlertHeight(Alert alert)
	{
		alert.getDialogPane().getChildren().stream().filter(node -> node instanceof Label).forEach(node -> ((Label) node).setMinHeight(Region.USE_PREF_SIZE));
	}
}

package de.subcentral.fx;

import java.util.function.Predicate;

import javafx.geometry.Pos;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.HBox;

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
}

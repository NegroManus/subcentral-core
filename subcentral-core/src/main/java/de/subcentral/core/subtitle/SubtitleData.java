package de.subcentral.core.subtitle;

import java.util.ArrayList;
import java.util.List;

public class SubtitleData
{
	private List<Item>	items	= new ArrayList<>();

	public List<Item> getItems()
	{
		return items;
	}

	public void setItems(List<Item> items)
	{
		this.items.clear();
		this.items.addAll(items);
	}
}

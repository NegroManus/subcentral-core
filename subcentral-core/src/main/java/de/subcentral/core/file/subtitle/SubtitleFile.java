package de.subcentral.core.file.subtitle;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;

public class SubtitleFile
{
    private List<Item> items = new ArrayList<>();

    public List<Item> getItems()
    {
	return items;
    }

    public void setItems(List<Item> items)
    {
	this.items.clear();
	this.items.addAll(items);
    }

    @Override
    public String toString()
    {
	return MoreObjects.toStringHelper(SubtitleFile.class).add("items", Joiner.on('\n').join(items)).toString();
    }
}

package de.subcentral.core.file.subtitle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;

public class SubtitleContent {
	private final List<Item> items;

	public SubtitleContent() {
		items = new ArrayList<>();
	}

	public SubtitleContent(Collection<? extends Item> items) {
		this.items = new ArrayList<>(items);
	}

	public List<Item> getItems() {
		return items;
	}

	public void setItems(Collection<? extends Item> items) {
		this.items.clear();
		this.items.addAll(items);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(SubtitleContent.class).add("items", Joiner.on('\n').join(items)).toString();
	}
}

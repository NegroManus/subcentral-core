package de.subcentral.fx.settings;

import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;

public class SimpleSettingsItem<T> implements SettingsItem<T> {
	protected final T item;

	public SimpleSettingsItem(T item) {
		this.item = Objects.requireNonNull(item, "item");
	}

	@Override
	public T getItem() {
		return item;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass().equals(obj.getClass())) {
			SimpleSettingsItem<?> o = (SimpleSettingsItem<?>) obj;
			return item.equals(o.item);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(71, 913).append(getClass()).append(item).toHashCode();
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(getClass()).add("item", item).toString();
	}
}

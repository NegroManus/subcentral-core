package de.subcentral.core.standardizing;

import java.util.List;

public interface Standardizer<T>
{
	public void standardize(T bean, List<StandardizingChange> changes);
}

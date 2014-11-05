package de.subcentral.core.standardizing;

import java.util.List;

public interface Standardizer<T>
{
	public List<StandardizingChange> standardize(T entity);
}

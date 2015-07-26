package de.subcentral.core.standardizing;

import java.util.List;

public interface StandardizingService
{
	public String getDomain();

	public List<StandardizingChange> standardize(Object bean);
}

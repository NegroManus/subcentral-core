package de.subcentral.core.lookup;

import java.util.List;

public interface LookupQuery<T>
{
	public List<T> execute() throws LookupException;
}

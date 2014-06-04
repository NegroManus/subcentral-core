package de.subcentral.core.naming;

public interface Nameable
{
	// Supposed to be unique. e.g. for Dallas 2012 title would be "Dallas", name would be "Dallas (2012)"
	public String getName();

	public String computeName();

	public default String getNameOrCompute()
	{
		return getName() != null ? getName() : computeName();
	}

}

package de.subcentral.core.metadata;

import com.google.common.collect.ComparisonChain;

import de.subcentral.core.Constants;

public interface Contributor extends Comparable<Contributor>
{
	public String getName();

	@Override
	public default int compareTo(Contributor o)
	{
		// nulls first
		if (o == null)
		{
			return 1;
		}
		return ComparisonChain.start().compare(getName(), o.getName(), Constants.STRING_ORDERING).result();
	}
}

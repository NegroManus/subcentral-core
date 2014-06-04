package de.subcentral.core.naming;

import java.util.Comparator;

public class Nameables
{
	public static Comparator<Nameable>	NAME_COMPARATOR	= new NameComparator();

	private static class NameComparator implements Comparator<Nameable>
	{
		@Override
		public int compare(Nameable o1, Nameable o2)
		{
			if (o1 == null)
			{
				return o2 == null ? 0 : -1;
			}
			if (o2 == null)
			{
				// o1 is not null here
				return 1;
			}
			if (o1.getNameOrCompute() == null)
			{
				return o2.getNameOrCompute() == null ? 0 : -1;
			}
			if (o2.getNameOrCompute() == null)
			{
				// o1.getName() is not null here
				return 1;
			}
			return o1.getNameOrCompute().compareTo(o2.getNameOrCompute());
		}
	}

	private Nameables()
	{
		// utility class
	}
}

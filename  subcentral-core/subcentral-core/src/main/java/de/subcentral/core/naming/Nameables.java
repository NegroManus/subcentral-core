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
			if (o1.getName() == null)
			{
				return o2.getName() == null ? 0 : -1;
			}
			if (o2.getName() == null)
			{
				// o1.getName() is not null here
				return 1;
			}
			return o1.getName().compareTo(o2.getName());
		}
	}

	private Nameables()
	{
		// utility class
	}
}

package de.subcentral.core.naming;

import java.util.Comparator;

public class Nameables
{
	public static Comparator<Named>	NAME_COMPARATOR	= new NameComparator();

	private static class NameComparator implements Comparator<Named>
	{
		@Override
		public int compare(Named o1, Named o2)
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

	public static String name(Named n, NamingService ns) throws NamingException
	{
		if (n.isNameSet())
		{
			return n.getName();
		}
		if (ns != null)
		{
			try
			{
				return ns.name(n);
			}
			catch (NoNamerRegisteredException e)
			{
				// do nothing;
			}
		}
		return n.computeName();
	}

	private Nameables()
	{
		// utility class
	}
}

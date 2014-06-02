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

	public static void appendSpace(StringBuilder sb)
	{
		int len = sb.length();
		if (len > 0 && sb.charAt(len - 1) != ' ')
		{
			sb.append(' ');
		}
	}

	public static void deleteTrailingSpaces(StringBuilder sb)
	{
		int len = sb.length();
		while (len > 0 && sb.charAt(len - 1) == ' ')
		{
			sb.deleteCharAt(len - 1);
			len = sb.length();
		}
	}

	private Nameables()
	{
		// utility class
	}
}

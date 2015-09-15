package de.subcentral.core.metadata.media;

import java.util.Iterator;

public class MediaUtil
{
	public static boolean isMediaIterable(Object obj)
	{
		if (obj instanceof Iterable)
		{
			for (Object o : (Iterable<?>) obj)
			{
				if (!(o instanceof Media))
				{
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public static boolean isSingletonMediaIterable(Object obj)
	{
		return getMediaOfSingletonIterable(obj) != null;
	}

	public static Media getMediaOfSingletonIterable(Object obj)
	{
		if (obj instanceof Iterable)
		{
			Iterator<?> iter = ((Iterable<?>) obj).iterator();
			if (iter.hasNext())
			{
				Object firstElem = iter.next();
				if (firstElem instanceof Media && !iter.hasNext())
				{
					return (Media) firstElem;
				}
			}
		}
		return null;
	}

	private MediaUtil()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}

}

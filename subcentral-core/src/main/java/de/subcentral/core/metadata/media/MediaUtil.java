package de.subcentral.core.metadata.media;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.ImmutableList;

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
		return getSingletonMediaFromIterable(obj) != null;
	}

	public static Media getSingletonMediaFromIterable(Object obj)
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

	public static List<String> getAllNames(NamedMedia media)
	{
		ImmutableList.Builder<String> names = ImmutableList.builder();
		if (media.getName() != null)
		{
			names.add(media.getName());
		}
		names.addAll(media.getAliasNames());
		return names.build();
	}

	private MediaUtil()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}

}

package de.subcentral.core.metadata.media;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.subcentral.core.naming.AbstractNamedMediaNamer;

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

	public static boolean isSingletonMedia(Object obj)
	{
		return toSingletonMedia(obj) != null;
	}

	public static Media toSingletonMedia(Object obj)
	{
		if (obj instanceof Media)
		{
			return (Media) obj;
		}
		Iterator<?> iter;
		if (obj instanceof Iterable)
		{
			iter = ((Iterable<?>) obj).iterator();
		}
		else if (obj instanceof Stream)
		{
			iter = ((Stream<?>) obj).iterator();
		}
		else
		{
			iter = null;
		}
		if (iter != null && iter.hasNext())
		{
			Object firstElem = iter.next();
			if (firstElem instanceof Media && !iter.hasNext())
			{
				return (Media) firstElem;
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

	public static List<Map<String, Object>> generateNamingParametersForAllNames(Object obj)
	{
		Media singleMedia = toSingletonMedia(obj);
		if (singleMedia != null)
		{
			if (obj instanceof NamedMedia)
			{
				NamedMedia namedMedia = (NamedMedia) obj;
				if (namedMedia.getName() != null)
				{
					return generateNamingParametersForMediaNames(((NamedMedia) obj).getAllNames());
				}
			}
		}
		MultiEpisodeHelper meHelper = MultiEpisodeHelper.of(obj);
		if (meHelper != null && meHelper.getCommonSeries() != null && meHelper.getCommonSeries().getName() != null)
		{
			return generateNamingParametersForMediaNames(meHelper.getCommonSeries().getAllNames());
		}

		// The list has to at least contain 1 entry (an empty map) because otherwise no names are generated
		return ImmutableList.of(ImmutableMap.of());
	}

	public static List<Map<String, Object>> generateNamingParametersForMediaNames(List<String> names)
	{
		ImmutableList.Builder<Map<String, Object>> params = ImmutableList.builder();
		for (String name : names)
		{
			params.add(ImmutableMap.of(AbstractNamedMediaNamer.PARAM_NAME, name));
		}
		return params.build();
	}

	private MediaUtil()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}

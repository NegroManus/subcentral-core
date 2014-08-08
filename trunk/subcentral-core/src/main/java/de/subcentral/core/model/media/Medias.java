package de.subcentral.core.model.media;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import de.subcentral.core.Settings;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.naming.Namings;
import de.subcentral.core.naming.NoNamerRegisteredException;

public class Medias
{
	public static final Comparator<Media>	MEDIA_NAME_COMPARATOR	= new MediaNameComparator();

	private static final class MediaNameComparator implements Comparator<Media>
	{
		@Override
		public int compare(Media o1, Media o2)
		{
			// nulls last
			if (o1 == null)
			{
				return o2 == null ? 0 : 1;
			}
			if (o2 == null)
			{
				return -1;
			}
			return Settings.STRING_ORDERING.compare(o1.getName(), o2.getName());
		}
	}

	public static MultiEpisodeHelper newMultiEpisode(List<? extends Media> media)
	{
		MultiEpisodeHelper me = new MultiEpisodeHelper(media.size());
		for (Media m : media)
		{
			if (m instanceof Episode)
			{
				me.add((Episode) m);
				continue;
			}
			return null;
		}
		return me;
	}

	public static String name(List<? extends Media> media, NamingService namingService, Map<String, Object> parameters, String mediaSeparator)
	{
		int numOfMedia = media.size();
		if (numOfMedia == 0)
		{
			return "";
		}
		else if (numOfMedia == 1)
		{
			return namingService.name(media.get(0), parameters);
		}
		else
		{
			try
			{
				MultiEpisodeHelper me = MultiEpisodeHelper.of(media);
				return namingService.name(me, parameters);
			}
			catch (IllegalArgumentException | NoNamerRegisteredException e)
			{
				// IAE if media is not a list of Episodes
				// NNRE if namingService has no namer registered for MultiEpisodeHelper
				return Namings.name(media, namingService, parameters, mediaSeparator);
			}
		}
	}

	public static void validateMediaContentType(String[] allowedTypes, String mediaContentType) throws IllegalArgumentException
	{
		if (mediaContentType == null || ArrayUtils.contains(allowedTypes, mediaContentType))
		{
			return;
		}
		throw new IllegalArgumentException("mediaContentType must be null or one of " + allowedTypes);
	}

	private Medias()
	{
		// utility class
	}
}

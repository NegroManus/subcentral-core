package de.subcentral.core.media;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.common.base.Joiner;

import de.subcentral.core.naming.NamingService;
import de.subcentral.core.naming.NoNamerRegisteredException;
import de.subcentral.core.util.Settings;

public class Medias
{
	public static final Comparator<Media>	MEDIA_NAME_COMPARATOR	= new MediaNameComparator();

	static final class MediaNameComparator implements Comparator<Media>
	{
		@Override
		public int compare(Media o1, Media o2)
		{
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

	public static MultiEpisode newMultiEpisode(List<? extends Media> media)
	{
		MultiEpisode me = new MultiEpisode(media.size());
		for (Media m : media)
		{
			if (m instanceof Episode)
			{
				me.add((Episode) m);
			}
			return null;
		}
		return me;
	}

	public static String name(List<? extends Media> media, NamingService namingService, String mediaSeparator)
	{
		int numOfMedia = media.size();
		if (numOfMedia == 0)
		{
			return "";
		}
		else if (numOfMedia == 1)
		{
			return namingService.name(media.get(0));
		}
		else
		{
			try
			{
				MultiEpisode me = MultiEpisode.of(media);
				return namingService.name(me);
			}
			catch (IllegalArgumentException | NoNamerRegisteredException e)
			{
				List<String> names = new ArrayList<>(media.size());
				for (Media m : media)
				{
					names.add(namingService.name(m));
				}
				return Joiner.on(mediaSeparator).join(names);
			}
		}
	}

	private Medias()
	{
		// utility class
	}
}

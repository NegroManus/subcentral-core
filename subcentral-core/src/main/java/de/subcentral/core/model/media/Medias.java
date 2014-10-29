package de.subcentral.core.model.media;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

import de.subcentral.core.Settings;
import de.subcentral.core.util.ListComparator;

public class Medias
{
	public static final Comparator<Media>		MEDIA_NAME_COMPARATOR		= new MediaNameComparator();
	public static final Comparator<List<Media>>	MEDIA_LIST_NAME_COMPARATOR	= ListComparator.create(Medias.MEDIA_NAME_COMPARATOR);

	private static final class MediaNameComparator implements Comparator<Media>, Serializable
	{
		// Comparators should be Serializable
		private static final long	serialVersionUID	= -3197188465533525469L;

		@Override
		public int compare(Media o1, Media o2)
		{
			// nulls first
			if (o1 == null)
			{
				return o2 == null ? 0 : -1;
			}
			if (o2 == null)
			{
				return 1;
			}
			return Settings.STRING_ORDERING.compare(o1.getName(), o2.getName());
		}
	}

	private Medias()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore should not be instantiated");
	}
}
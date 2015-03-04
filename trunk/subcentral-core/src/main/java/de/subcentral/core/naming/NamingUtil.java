package de.subcentral.core.naming;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;

import de.subcentral.core.Settings;
import de.subcentral.core.metadata.media.Media;
import de.subcentral.core.util.IterableComparator;

public class NamingUtil
{
	public static final Comparator<Media>			DEFAULT_MEDIA_NAME_COMPARATOR			= new MediaNameComparator(NamingDefaults.getDefaultNamingService());
	public static final Comparator<Iterable<Media>>	DEFAULT_MEDIA_ITERABLE_NAME_COMPARATOR	= IterableComparator.create(DEFAULT_MEDIA_NAME_COMPARATOR);

	public static final class MediaNameComparator implements Comparator<Media>, Serializable
	{
		// Comparators should be Serializable
		private static final long	serialVersionUID	= -3197188465533525469L;

		private final NamingService	namingService;

		public MediaNameComparator(NamingService namingService)
		{
			this.namingService = Objects.requireNonNull(namingService, "namingService");
		}

		@Override
		public int compare(Media o1, Media o2)
		{
			// nulls first as naming of null results in an empty string "" and an empty string always comes first
			return Settings.STRING_ORDERING.compare(namingService.name(o1), namingService.name(o2));
		}
	}

	public static final <T> T readParameter(Map<String, Object> parameters, String key, Class<T> valueClass, T defaultValue)
	{
		return valueClass.cast(parameters.getOrDefault(key, defaultValue));
	}

	private NamingUtil()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}

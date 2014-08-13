package de.subcentral.core.standardizing;

import com.google.common.collect.ImmutableListMultimap;

import de.subcentral.core.model.release.Release;
import de.subcentral.core.model.release.Releases;
import de.subcentral.core.model.subtitle.Subtitle;
import de.subcentral.core.model.subtitle.Subtitles;

public class Standardizings
{
	private static final SimpleStandardizingService	DEFAULT_STANDARDIZING_SERVICE	= new SimpleStandardizingService();
	static
	{
		ImmutableListMultimap.Builder<Class<?>, Standardizer<?>> standardizers = ImmutableListMultimap.builder();
		standardizers.put(Subtitle.class, (Subtitle s) -> Subtitles.standardizeTags(s));
		standardizers.put(Release.class, (Release r) -> Releases.standardizeTags(r));
		DEFAULT_STANDARDIZING_SERVICE.setStandardizers(standardizers.build());
	}

	public static StandardizingService getDefaultStandardizingService()
	{
		return DEFAULT_STANDARDIZING_SERVICE;
	}

	public static <T> T mayStandardize(T entity, StandardizingService standardizingService)
	{
		if (standardizingService != null)
		{
			return standardizingService.standardize(entity);
		}
		return entity;
	}

	private Standardizings()
	{
		// utility class
	}

}

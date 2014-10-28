package de.subcentral.core.standardizing;

import com.google.common.collect.ImmutableListMultimap;

import de.subcentral.core.model.release.Release;
import de.subcentral.core.model.release.Releases;
import de.subcentral.core.model.subtitle.Subtitle;
import de.subcentral.core.model.subtitle.Subtitles;

public class Standardizings
{
	private static final SimpleStandardizingService	DEFAULT_STANDARDIZING_SERVICE	= new SimpleStandardizingService("default");
	static
	{
		ImmutableListMultimap.Builder<Class<?>, Standardizer<?>> standardizers = ImmutableListMultimap.builder();
		Standardizer<Subtitle> subStdzer = Subtitles::standardizeTags;
		Standardizer<Release> rlsStdzer = Releases::standardizeTags;
		standardizers.put(Subtitle.class, subStdzer);
		standardizers.put(Release.class, rlsStdzer);
		DEFAULT_STANDARDIZING_SERVICE.setStandardizers(standardizers.build());
	}

	public static StandardizingService getDefaultStandardizingService()
	{
		return DEFAULT_STANDARDIZING_SERVICE;
	}

	public static <T> void mayStandardize(T entity, StandardizingService standardizingService)
	{
		if (standardizingService != null)
		{
			standardizingService.standardize(entity);
		}
	}

	private Standardizings()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore should not be instantiated.");
	}
}

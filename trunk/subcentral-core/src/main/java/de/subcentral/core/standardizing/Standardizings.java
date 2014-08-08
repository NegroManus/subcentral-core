package de.subcentral.core.standardizing;

import de.subcentral.core.model.release.Release;
import de.subcentral.core.model.release.Releases;
import de.subcentral.core.model.subtitle.Subtitle;
import de.subcentral.core.model.subtitle.Subtitles;

public class Standardizings
{
	private static final SimpleStandardizingService	DEFAULT_STANDARDIZING_SERVICE	= new SimpleStandardizingService();
	static
	{
		DEFAULT_STANDARDIZING_SERVICE.registerStandardizer(Subtitle.class, Subtitles::standardizeTags);
		DEFAULT_STANDARDIZING_SERVICE.registerStandardizer(Release.class, Releases::standardizeTags);
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

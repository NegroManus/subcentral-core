package de.subcentral.core.standardizing;

import java.util.ArrayList;
import java.util.List;

import de.subcentral.core.model.media.Episode;
import de.subcentral.core.model.release.Release;
import de.subcentral.core.model.release.Releases;
import de.subcentral.core.model.subtitle.Subtitle;
import de.subcentral.core.model.subtitle.Subtitles;

public class Standardizings
{
	private static final ClassBasedStandardizingService	DEFAULT_STANDARDIZING_SERVICE	= new ClassBasedStandardizingService("default");
	static
	{
		DEFAULT_STANDARDIZING_SERVICE.registerStandardizer(Subtitle.class, Subtitles::standardizeTags);
		DEFAULT_STANDARDIZING_SERVICE.registerStandardizer(Release.class, Releases::standardizeTags);

		DEFAULT_STANDARDIZING_SERVICE.addNestedBeanRetriever(Release.class, r -> r.getMedia());
		DEFAULT_STANDARDIZING_SERVICE.addNestedBeanRetriever(Episode.class, e -> {
			List<Object> nestedBeans = new ArrayList<>(2);
			if (e.getSeries() != null)
			{
				nestedBeans.add(e.getSeries());
			}
			if (e.getSeason() != null)
			{
				nestedBeans.add(e.getSeason());
			}
			return nestedBeans;
		});
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

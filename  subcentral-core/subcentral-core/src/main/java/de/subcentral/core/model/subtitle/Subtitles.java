package de.subcentral.core.model.subtitle;

import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import de.subcentral.core.model.release.MediaRelease;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.naming.SubtitleReleaseNamer;

public class Subtitles
{
	public Set<String> generateNames(SubtitleRelease subRls, NamingService namingService)
	{
		ImmutableSet.Builder<String> names = ImmutableSet.builder();
		for (MediaRelease rls : subRls.getMatchingMediaReleases())
		{
			names.add(namingService.name(subRls, ImmutableMap.of(SubtitleReleaseNamer.PARAM_MEDIA_KEY_RELEASE, rls)));
		}
		return names.build();
	}

	private Subtitles()
	{
		// utility class
	}

}

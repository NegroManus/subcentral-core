package de.subcentral.core.model.subtitle;

import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import de.subcentral.core.model.release.Release;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.naming.SubtitleAdjustmentNamer;

public class Subtitles
{
	public Set<String> generateNames(SubtitleAdjustment subRls, NamingService namingService)
	{
		ImmutableSet.Builder<String> names = ImmutableSet.builder();
		for (Release rls : subRls.getMatchingReleases())
		{
			names.add(namingService.name(subRls, ImmutableMap.of(SubtitleAdjustmentNamer.PARAM_KEY_RELEASE, rls)));
		}
		return names.build();
	}

	private Subtitles()
	{
		// utility class
	}

}

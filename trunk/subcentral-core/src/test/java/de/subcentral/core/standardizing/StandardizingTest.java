package de.subcentral.core.standardizing;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import de.subcentral.core.model.media.Episode;
import de.subcentral.core.model.release.Release;

public class StandardizingTest
{
	@Test
	public void testStandardizing()
	{
		StandardizingService service = Standardizings.getDefaultStandardizingService();

		Release rls = Release.create(Episode.createSeasonedEpisode("Psych", 5, 6), "CtrlHD", "720p", "WEB-DL", "H", "264", "DD5", "1");

		List<StandardizingChange> changes = service.standardize(rls);
		changes.stream().forEach(c -> System.out.println(c));
	}

	@Test
	public void testReflectiveStandardizing()
	{
		ReflectiveStandardizer<Episode> stdzer = new ReflectiveStandardizer<>(Episode.class, ImmutableMap.of("numberInSeason", (Integer n) -> n + 1));

		Episode epi = Episode.createSeasonedEpisode("Psych", 2, 2);

		List<StandardizingChange> changes = stdzer.standardize(epi);
		changes.stream().forEach(c -> System.out.println(c));
	}
}

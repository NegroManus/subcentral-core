package de.subcentral.core.metadata.release;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import de.subcentral.core.metadata.media.Episode;

public class CompatibilityServiceTest
{
	@Test
	public void testFindCompatibles()
	{
		Episode epi = Episode.createSeasonedEpisode("Psych", 1, 1);
		Release sourceRls = Release.create("Psych.S01E01.HDTV.x264-LOL", epi, "LOL", "HDTV", "x264");

		CompatibilityService compService = new CompatibilityService();
		compService.getRules().add(new SameGroupCompatibilityRule());
		compService.getRules().add(new CrossGroupCompatibilityRule(new Group("LOL"), new Group("DIMENSION"), true));
		List<Release> existingRlss = new ArrayList<>(4);
		existingRlss.add(Release.create("Psych.S01E01.HDTV.x264-LOL", epi, "LOL", "HDTV", "x264"));
		existingRlss.add(Release.create("Psych.S01E01.PROPER.HDTV.x264-LOL", epi, "LOL", "PROPER", "HDTV", "x264"));
		existingRlss.add(Release.create("Psych.S01E01.720p.HDTV.x264-DIMENSION", epi, "DIMENSION", "720p", "HDTV", "x264"));
		existingRlss.add(Release.create("Psych.S01E01.720p.WEB-DL.H.264.DD5.1-KiNGS", epi, "KiNGS", "720p", "WEB-DL", "H.264", "DD5.1"));

		Set<Release> expectedCompatibleRlss = new HashSet<>();
		expectedCompatibleRlss.add(existingRlss.get(1)); // PROPER-LOL
		expectedCompatibleRlss.add(existingRlss.get(2)); // DIMENSION

		Set<Compatibility> compatibilities = compService.findCompatibilities(sourceRls, existingRlss);
		Set<Release> compatibleReleases = compatibilities.stream().map(Compatibility::getCompatible).collect(Collectors.toSet());
		compatibilities.forEach(e -> System.out.println(e));

		assertEquals(expectedCompatibleRlss, compatibleReleases);
	}
}

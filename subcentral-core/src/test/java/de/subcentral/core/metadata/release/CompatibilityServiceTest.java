package de.subcentral.core.metadata.release;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.release.CompatibilityService;
import de.subcentral.core.metadata.release.Group;
import de.subcentral.core.metadata.release.GroupsCompatibility;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.SameGroupCompatibility;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.metadata.release.CompatibilityService.CompatibilityInfo;
import de.subcentral.core.metadata.release.GroupsCompatibility.Condition;

public class CompatibilityServiceTest
{
	@Test
	public void testFindCompatibles()
	{
		Episode epi = Episode.createSeasonedEpisode("Psych", 1, 1);
		Release sourceRls = Release.create("Psych.S01E01.HDTV.x264-LOL", epi, "LOL", "HDTV", "x264");

		CompatibilityService compService = new CompatibilityService();
		compService.getCompatibilities().add(new SameGroupCompatibility());
		compService.getCompatibilities().add(new GroupsCompatibility(new Group("LOL"), new Group("DIMENSION"), Condition.IF_EXISTS, true));
		compService.getCompatibilities().add(new GroupsCompatibility(Tag.list("720p", "HDTV", "x264"),
				null,
				Tag.list("HDTV", "XviD"),
				new Group("AFG"),
				Condition.ALWAYS,
				false));

		List<Release> existingRlss = new ArrayList<>(4);
		existingRlss.add(Release.create("Psych.S01E01.HDTV.x264-LOL", epi, "LOL", "HDTV", "x264"));
		existingRlss.add(Release.create("Psych.S01E01.PROPER.HDTV.x264-LOL", epi, "LOL", "PROPER", "HDTV", "x264"));
		existingRlss.add(Release.create("Psych.S01E01.720p.HDTV.x264-DIMENSION", epi, "DIMENSION", "720p", "HDTV", "x264"));
		existingRlss.add(Release.create("Psych.S01E01.720p.WEB-DL.H.264.DD5.1-KiNGS", epi, "KiNGS", "720p", "WEB-DL", "H.264", "DD5.1"));

		Set<Release> expectedCompatibleRlss = new HashSet<>();
		expectedCompatibleRlss.add(existingRlss.get(1)); // PROPER-LOL
		expectedCompatibleRlss.add(existingRlss.get(2)); // DIMENSION
		expectedCompatibleRlss.add(Release.create(epi, "AFG", "HDTV", "XviD")); // AFG

		Map<Release, CompatibilityInfo> compatibleRlss = compService.findCompatibles(sourceRls, existingRlss);
		compatibleRlss.entrySet().forEach(e -> System.out.println(e));

		assertEquals(expectedCompatibleRlss, compatibleRlss.keySet());
	}
}

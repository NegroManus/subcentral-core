package de.subcentral.core.model.release;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import de.subcentral.core.model.media.Episode;
import de.subcentral.core.model.release.Compatibility.Scope;

public class ReleasesTest
{

	@Test
	public void testStandardizeTags()
	{
		Release rls = new Release();
		rls.setTags(Tag.list("720", "WEB-DL", "DD5", "1", "x264"));
		Releases.standardizeTags(rls);
		assertEquals(Tag.list("720", "WEB-DL", "DD5.1", "x264"), rls.getTags());
	}

	@Test
	public void testFindCompatibleReleases()
	{
		Episode epi = Episode.createSeasonedEpisode("Psych", 1, 1);
		Release sourceRls = Release.create("Psych.S01E01.HDTV.x264-LOL", epi, "LOL", "HDTV", "x264");

		Set<Compatibility> compatibilities = new HashSet<>(2);
		compatibilities.add(new Compatibility(new Group("LOL"), new Group("DIMENSION"), Scope.IF_EXISTS, true));
		compatibilities.add(new Compatibility(null, Tag.list("720p", "HDTV", "x264"), new Group("AFG"), Tag.list("HDTV", "XviD"), Scope.ALWAYS, false));

		List<Release> existingRlss = new ArrayList<>(3);
		existingRlss.add(Release.create("Psych.S01E01.HDTV.x264-LOL", epi, "LOL", "HDTV", "x264"));
		existingRlss.add(Release.create("Psych.S01E01.720p.HDTV.x264-DIMENSION", epi, "DIMENSION", "720p", "HDTV", "x264"));
		existingRlss.add(Release.create("Psych.S01E01.720p.WEB-DL.H.264.DD5.1-KiNGS", epi, "KiNGS", "720p", "WEB-DL", "H.264", "DD5.1"));

		Map<Release, Compatibility> compatibleRlss = Releases.findCompatibleReleases(sourceRls, compatibilities, existingRlss);
		Set<Release> expectedCompatibleRlss = new HashSet<>();
		expectedCompatibleRlss.add(existingRlss.get(1)); // DIMENSION
		expectedCompatibleRlss.add(Release.create(epi, "AFG", "HDTV", "XviD")); // AFG
		compatibleRlss.entrySet().forEach(e -> System.out.println(e));
		assertEquals(expectedCompatibleRlss, compatibleRlss.keySet());
	}
}

package de.subcentral.core.standardizing;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.media.Series;
import de.subcentral.core.metadata.release.Release;

public class StandardizingTest
{
	@Test
	public void testDefaultStandardizingService()
	{
		StandardizingService service = StandardizingDefaults.getDefaultStandardizingService();

		Release rls = Release.create(Episode.createSeasonedEpisode("Psych", 5, 6), "CtrlHD", "720p", "WEB-DL", "H", "264", "DD5", "1");
		Release expectedRls = Release.create(Episode.createSeasonedEpisode("Psych", 5, 6), "CtrlHD", "720p", "WEB-DL", "H.264", "DD5.1");

		List<StandardizingChange> changes = service.standardize(rls);
		changes.stream().forEach(c -> System.out.println(c));
		assertEquals(expectedRls, rls);
	}

	@Test
	public void testCustomStandardizingService()
	{
		TypeStandardizingService service = new TypeStandardizingService("test");
		StandardizingDefaults.registerAllDefaultNestedBeansRetrievers(service);
		service.registerStandardizer(Episode.class, (e, changes) -> {
			if (!e.isSpecial())
			{
				e.setSpecial(true);
				changes.add(new StandardizingChange(e, Episode.PROP_SPECIAL.getPropName(), false, true));
			}

		});
		service.registerStandardizer(Series.class, new SeriesNameStandardizer(Pattern.compile("Psych"), "Psych (2001)", "Psych"));

		Episode epi = Episode.createSeasonedEpisode("Psych", 2, 2);
		Episode expectedEpi = Episode.createSeasonedEpisode("Psych (2001)", 2, 2);
		expectedEpi.setSpecial(true);

		List<StandardizingChange> changes = service.standardize(epi);
		changes.stream().forEach(c -> System.out.println(c));

		assertEquals(expectedEpi, epi);
	}

	@Test
	public void testReflectiveStandardizing()
	{
		ReflectiveStandardizer<Series, String> stdzer = new ReflectiveStandardizer<>(Series.class,
				"name",
				(String name) -> StringUtils.upperCase(name));

		Series series = new Series("Psych");
		Series expectedSeries = new Series("PSYCH");

		List<StandardizingChange> changes = new ArrayList<>();
		stdzer.standardize(series, changes);
		changes.stream().forEach(c -> System.out.println(c));

		assertEquals(expectedSeries, series);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testReflectiveStandardizingFail()
	{
		ReflectiveStandardizer<Series, String> stdzer = new ReflectiveStandardizer<>(Series.class, "notExistingProp", (String s) -> s);
		List<StandardizingChange> changes = new ArrayList<>();
		stdzer.standardize(new Series("Psych"), changes);
	}
}

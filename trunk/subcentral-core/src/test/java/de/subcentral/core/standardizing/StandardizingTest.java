package de.subcentral.core.standardizing;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

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
		service.registerStandardizer(Episode.class, e -> {
			Boolean oldVal = Boolean.valueOf(e.isSpecial());
			e.setSpecial(true);
			Boolean newVal = Boolean.valueOf(e.isSpecial());
			return ImmutableList.of(new StandardizingChange(e, Episode.PROP_SPECIAL.getPropName(), oldVal, newVal));
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
		ReflectiveStandardizer<Series> stdzer = new ReflectiveStandardizer<Series>(Series.class, ImmutableMap.of("name",
				(String name) -> StringUtils.upperCase(name)));

		Series series = new Series("Psych");
		Series expectedSeries = new Series("PSYCH");

		List<StandardizingChange> changes = stdzer.standardize(series);
		changes.stream().forEach(c -> System.out.println(c));

		assertEquals(expectedSeries, series);
	}

	@Test(expected = StandardizingException.class)
	public void testReflectiveStandardizingFail()
	{
		ReflectiveStandardizer<Series> stdzer = new ReflectiveStandardizer<Series>(Series.class, ImmutableMap.of("notExistingProp", (String s) -> s));
		stdzer.standardize(new Series("Psych"));
	}
}

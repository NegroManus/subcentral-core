package de.subcentral.core.correction;

import static org.junit.Assert.assertEquals;

import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.media.Series;
import de.subcentral.core.metadata.release.Release;

public class CorrectionTest
{
	private CorrectionService service = CorrectionDefaults.getDefaultCorrectionService();

	@Test
	public void testDefaultCorrect01()
	{
		Release rls = Release.create(Episode.createSeasonedEpisode("Psych", 5, 6), "CtrlHD", "720p", "WEB-DL", "H", "264", "DD5", "1");
		Release expectedRls = Release.create(Episode.createSeasonedEpisode("Psych", 5, 6), "CtrlHD", "720p", "WEB-DL", "H.264", "DD5.1");

		List<Correction> changes = service.correct(rls);
		changes.stream().forEach(c -> System.out.println(c));
		assertEquals(expectedRls, rls);
	}

	@Test
	public void testDefaultCorrect02()
	{
		Release rls = Release.create("CtrlHD", "720p", "WEB-DL", "H", "264", "DD5", "1");
		Release expectedRls = Release.create("CtrlHD", "720p", "WEB-DL", "H.264", "DD5.1");

		List<Correction> changes = service.correct(rls);
		changes.forEach(c -> System.out.println(c));
		assertEquals(expectedRls, rls);
	}

	@Test
	public void testCustomCorrect()
	{
		TypeBasedCorrectionService service = new TypeBasedCorrectionService("test");
		CorrectionDefaults.registerAllDefaultNestedBeansRetrievers(service);
		service.registerCorrector(Episode.class, (e, changes) ->
		{
			if (!e.isSpecial())
			{
				e.setSpecial(true);
				changes.add(new Correction(e, Episode.PROP_SPECIAL.getPropName(), false, true));
			}

		});
		service.registerCorrector(Series.class, new SeriesNameCorrector(Pattern.compile("Psych"), "Psych (2001)", ImmutableList.of(), "Psych"));

		Episode epi = Episode.createSeasonedEpisode("Psych", 2, 2);
		Episode expectedEpi = Episode.createSeasonedEpisode("Psych (2001)", 2, 2);
		expectedEpi.setSpecial(true);

		List<Correction> changes = service.correct(epi);
		changes.stream().forEach(c -> System.out.println(c));

		assertEquals(expectedEpi, epi);
	}

	@Test
	public void testCorrectReflectiveSuccess() throws IntrospectionException
	{
		Corrector<Series> stdzer = new ReflectiveCorrector<>(Series.PROP_NAME, (String name) -> StringUtils.upperCase(name));

		Series series = new Series("Psych");
		Series expectedSeries = new Series("PSYCH");

		List<Correction> changes = new ArrayList<>();
		stdzer.correct(series, changes);
		changes.stream().forEach(c -> System.out.println(c));

		assertEquals(expectedSeries, series);
	}

	@Test(expected = IntrospectionException.class)
	public void testCorrectReflectiveFail() throws IntrospectionException
	{
		Corrector<Series> stdzer = new ReflectiveCorrector<>(Series.class, "notExistingProp", (String s) -> s);
		List<Correction> changes = new ArrayList<>();
		stdzer.correct(new Series("Psych"), changes);
	}
}

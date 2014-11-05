package de.subcentral.core.standardizing;

import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.subcentral.core.model.media.Episode;
import de.subcentral.core.model.media.Season;
import de.subcentral.core.model.media.Series;
import de.subcentral.core.model.release.Release;
import de.subcentral.core.model.subtitle.Subtitle;
import de.subcentral.core.model.subtitle.SubtitleAdjustment;
import de.subcentral.core.util.PatternReplacer;

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
		ClassBasedStandardizingService service = new ClassBasedStandardizingService("test");
		service.registerNestedBeanRetriever(Episode.class, Standardizings::retrieveNestedBeans);
		service.registerNestedBeanRetriever(Season.class, Standardizings::retrieveNestedBeans);
		service.registerNestedBeanRetriever(Release.class, Standardizings::retrieveNestedBeans);
		service.registerNestedBeanRetriever(Subtitle.class, Standardizings::retrieveNestedBeans);
		service.registerNestedBeanRetriever(SubtitleAdjustment.class, Standardizings::retrieveNestedBeans);
		service.registerStandardizer(Episode.class, e -> {
			Boolean oldVal = Boolean.valueOf(e.isSpecial());
			e.setSpecial(true);
			return ImmutableList.of(new StandardizingChange(e, Episode.PROP_SPECIAL, oldVal, Boolean.TRUE));
		});
		service.registerStandardizer(Series.class,
				new SeriesNamerStandardizer(new PatternReplacer(ImmutableMap.<Pattern, String> of(Pattern.compile("Psych"), "Psych (2001)"))));

		Episode epi = Episode.createSeasonedEpisode("Psych", 2, 2);

		List<StandardizingChange> changes = service.standardize(epi);
		changes.stream().forEach(c -> System.out.println(c));
		System.out.println(epi);
	}
}

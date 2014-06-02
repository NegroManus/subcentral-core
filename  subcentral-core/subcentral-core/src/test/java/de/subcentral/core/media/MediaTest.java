package de.subcentral.core.media;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.release.Group;
import de.subcentral.core.release.MediaRelease;
import de.subcentral.core.release.Tag;
import de.subcentral.core.subtitle.Subtitle;

public class MediaTest
{
	public static void main(String[] args)
	{
		Series psych = new Series();
		psych.setTitle("Psych");
		psych.setOriginalLanguage("en");

		Season season1 = psych.addSeason();
		season1.setNumber(1);

		Episode epi1 = psych.addEpisode(season1);
		epi1.setNumberInSeason(1);
		epi1.setTitle("Pilot");

		Episode epi2 = psych.addEpisode(season1);
		epi2.setNumberInSeason(2);

		String name = epi1.getName();
		System.out.println(name);

		// Media release
		MediaRelease rel = new MediaRelease();
		rel.setMaterials(ImmutableList.of(epi1, epi2));
		Group lol = new Group();
		lol.setName("LOL");
		rel.setGroup(lol);
		Tag xvid = new Tag();
		xvid.setName("XviD");
		Tag hdtv = new Tag();
		hdtv.setName("HDTV");
		rel.setTags(ImmutableList.of(hdtv, xvid));

		System.out.println(rel.getName());

		// Subtitle release
		Subtitle sub1 = new Subtitle();
		sub1.setMedia(epi1);
		sub1.setLanguage("de");

		Subtitle sub2 = new Subtitle();
		sub2.setMedia(epi2);
		sub2.setLanguage("de");

		System.out.println(sub1.getName());
		System.out.println(sub1.isTranslation());

	}
}

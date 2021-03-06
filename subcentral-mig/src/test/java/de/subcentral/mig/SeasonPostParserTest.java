package de.subcentral.mig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import com.google.common.io.Resources;

import de.subcentral.core.metadata.Contribution;
import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.metadata.subtitle.SubtitleRelease;
import de.subcentral.mig.parse.SeasonPostParser;
import de.subcentral.mig.parse.SeasonPostParser.SeasonPostData;

public class SeasonPostParserTest {
    private SeasonPostParser parser = new SeasonPostParser();

    @Test
    public void testParsePsychS05() throws IOException {
        parse("Psych - Staffel 5 - [DE-Subs: 16 | VO-Subs: 16] - [Komplett]", "post-psych_s05.html");
    }

    @Test
    public void testParseCsiMiamiS07() throws IOException {
        parse("CSI: Miami - Staffel 7 - [DE-Subs: 25 | VO-Subs: 25] - [Komplett]", "post-csimiami_s07.html");
    }

    @Test
    public void testParseTheWalkingDeadS06() throws IOException {
        parse("The Walking Dead - Staffel 6 - [DE-Subs: 12 | VO-Subs: 14 | Aired: 14/16]", "post-thewalkingdead_s06.html");
    }

    @Test
    public void testParseOnceUponATimeS04() throws IOException {
        parse("Once Upon a Time - Staffel 4 - [DE-Subs: 23 | VO-Subs: 23] - [Komplett]", "post-onceuponatime_s04.html");
    }

    @Test
    public void testTheForgottenS01() throws IOException {
        parse("The Forgotten - Staffel 1 - [DE-Subs: 17 | VO-Subs: 17] - [Komplett]", "post-theforgotten_s01.html");
    }

    @Test
    public void testParseTopic() throws IOException {
        String s = "'Til Death - Staffel 1-2 - [DE-Subs: 15 | VO-Subs: 15] - [Komplett]";
        SeasonPostData data = parser.parsePostTopic(s);
        printSeasonPostData(data);
    }

    private void parse(String postTitle, String postContentResourceFilename) throws IOException {
        String content = Resources.toString(Resources.getResource(SeasonPostParserTest.class, postContentResourceFilename), StandardCharsets.UTF_8);
        SeasonPostData data = parser.parsePost(postTitle, content);
        printSeasonPostData(data);
    }

    private void printSeasonPostData(SeasonPostData data) {
        System.out.println();
        System.out.println("Series: " + data.getSeries());
        System.out.println("Seasons:");
        for (Season season : data.getSeasons()) {
            System.out.println(season);
        }

        System.out.println("Episodes:");
        for (Episode epi : data.getEpisodes()) {
            System.out.println(epi);
        }

        System.out.println();
        System.out.println("Subs:");
        for (SubtitleRelease subRls : data.getSubtitleReleases()) {
            System.out.println(subRls);
            if (subRls.getFirstSubtitle() != null) {
                for (Contribution c : subRls.getFirstSubtitle().getContributions()) {
                    System.out.println(c);
                }
            }
            for (Contribution c : subRls.getContributions()) {
                System.out.println(c);
            }
        }
    }
}

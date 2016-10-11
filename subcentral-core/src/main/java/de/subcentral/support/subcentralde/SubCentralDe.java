package de.subcentral.support.subcentralde;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.subcentral.core.correct.PatternStringReplacer;
import de.subcentral.core.correct.SubtitleLanguageCorrector;
import de.subcentral.core.correct.TypeBasedCorrectionService;
import de.subcentral.core.metadata.Site;
import de.subcentral.core.metadata.media.Media;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleRelease;
import de.subcentral.core.parse.CompoundMappingMatcher;
import de.subcentral.core.parse.CompoundMappingMatcher.GroupEntry;
import de.subcentral.core.parse.CompoundMappingMatcher.KeyEntry;
import de.subcentral.core.parse.CompoundMappingMatcher.MatcherEntry;
import de.subcentral.core.parse.Mapper;
import de.subcentral.core.parse.MappingMatcher;
import de.subcentral.core.parse.MultiMappingMatcher;
import de.subcentral.core.parse.Parser;
import de.subcentral.core.parse.ParsingService;
import de.subcentral.core.parse.ReleaseParser;
import de.subcentral.core.parse.SubtitleReleaseParser;
import de.subcentral.core.parse.TypeBasedParsingService;
import de.subcentral.core.parse.TypeBasedParsingService.ParserEntry;
import de.subcentral.core.util.SimplePropDescriptor;
import de.subcentral.support.releasescene.ReleaseScene;

public class SubCentralDe {
    private static final Logger                  log             = LogManager.getLogger(SubCentralDe.class.getName());

    private static final Site                    SITE            = new Site("subcentral.de", "SubCentral.de", "https://www.subcentral.de/");
    private static final TypeBasedParsingService PARSING_SERVICE = initParsingService();

    private SubCentralDe() {
        throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
    }

    @SuppressWarnings("unchecked")
    private static TypeBasedParsingService initParsingService() {
        ImmutableList.Builder<Parser<SubtitleRelease>> parsers = ImmutableList.builder();

        for (ParserEntry<?> sceneParserEntry : ReleaseScene.getParsersEntries()) {
            Parser<?> sceneParser = sceneParserEntry.getParser();
            if (sceneParser instanceof ReleaseParser) {
                ReleaseParser sceneRlsParser = (ReleaseParser) sceneParser;

                Mapper<List<Media>> mediaMapper;
                try {
                    mediaMapper = (Mapper<List<Media>>) sceneRlsParser.getMediaMapper();
                }
                catch (ClassCastException e) {
                    log.warn("Parser will be ignored because its media mapper does not map to List<Media>: {}", sceneRlsParser.getMediaMapper());
                    continue;
                }

                // Building the matchers for SubCentral:
                // The scene matchers will be the source for the SubCentral matchers
                // because all SubCentral names consist of the scene name of the release followed by SubCentral tags.
                MappingMatcher<SimplePropDescriptor> scMatcher = buildMatcherBasedOnSceneMatcher(sceneRlsParser.getMatcher());

                SubtitleReleaseParser parser = new SubtitleReleaseParser(scMatcher, mediaMapper);
                parsers.add(parser);
            }
            else {
                log.warn("Parser will be ignored because it is not an instance of ReleaseParser: {}", sceneParser);
            }
        }

        TypeBasedParsingService service = new TypeBasedParsingService(SITE.getName());
        service.registerAll(SubtitleRelease.class, parsers.build());
        return service;
    }

    private static MappingMatcher<SimplePropDescriptor> buildMatcherBasedOnSceneMatcher(MappingMatcher<SimplePropDescriptor> sceneMatcher) {
        String release = "(.*)";
        String lang = "\\W(de|ger|german|deutsch|VO|en|eng|english)";
        String group = "\\W([\\w&_]+)";
        String groupOpt = "(?:" + group + ")?";
        String version = "\\W(V\\d)";
        String versionOpt = "(?:" + version + ")?";

        ImmutableList.Builder<MappingMatcher<SimplePropDescriptor>> matchers = ImmutableList.builder();
        KeyEntry<SimplePropDescriptor> subRlsNameEntry = GroupEntry.ofKey(SubtitleRelease.PROP_NAME);
        KeyEntry<SimplePropDescriptor> subRlsVersionEntry = GroupEntry.ofKey(SubtitleRelease.PROP_VERSION);
        KeyEntry<SimplePropDescriptor> subLangEntry = GroupEntry.ofKey(Subtitle.PROP_LANGUAGE);
        KeyEntry<SimplePropDescriptor> subGroupEntry = GroupEntry.ofKey(Subtitle.PROP_GROUP);
        MatcherEntry<SimplePropDescriptor> sceneMatcherEntry = GroupEntry.ofMatcher(sceneMatcher);

        // Version, Language, (Group)?
        Pattern p101 = Pattern.compile(release + version + lang + groupOpt, Pattern.CASE_INSENSITIVE);
        ImmutableMap.Builder<Integer, GroupEntry<SimplePropDescriptor>> g101 = ImmutableMap.builder();
        g101.put(0, subRlsNameEntry);
        g101.put(1, sceneMatcherEntry);
        g101.put(2, subRlsVersionEntry);
        g101.put(3, subLangEntry);
        g101.put(4, subGroupEntry);
        MappingMatcher<SimplePropDescriptor> m101 = new CompoundMappingMatcher<>(p101, g101.build());
        matchers.add(m101);

        // Language, Version, (Group)?
        Pattern p102 = Pattern.compile(release + lang + version + groupOpt, Pattern.CASE_INSENSITIVE);
        ImmutableMap.Builder<Integer, GroupEntry<SimplePropDescriptor>> g102 = ImmutableMap.builder();
        g102.put(0, subRlsNameEntry);
        g102.put(1, sceneMatcherEntry);
        g102.put(2, subLangEntry);
        g102.put(3, subRlsVersionEntry);
        g102.put(4, subGroupEntry);
        MappingMatcher<SimplePropDescriptor> m2 = new CompoundMappingMatcher<>(p102, g102.build());
        matchers.add(m2);

        // Language, Group, (Version)?
        Pattern p103 = Pattern.compile(release + lang + group + versionOpt, Pattern.CASE_INSENSITIVE);
        ImmutableMap.Builder<Integer, GroupEntry<SimplePropDescriptor>> g103 = ImmutableMap.builder();
        g103.put(0, subRlsNameEntry);
        g103.put(1, sceneMatcherEntry);
        g103.put(2, subLangEntry);
        g103.put(3, subGroupEntry);
        g103.put(4, subRlsVersionEntry);
        MappingMatcher<SimplePropDescriptor> m103 = new CompoundMappingMatcher<>(p103, g103.build());
        matchers.add(m103);

        // Language, (Group)?
        Pattern p104 = Pattern.compile(release + lang + groupOpt, Pattern.CASE_INSENSITIVE);
        ImmutableMap.Builder<Integer, GroupEntry<SimplePropDescriptor>> g104 = ImmutableMap.builder();
        g104.put(0, subRlsNameEntry);
        g104.put(1, sceneMatcherEntry);
        g104.put(2, subLangEntry);
        g104.put(3, subGroupEntry);
        MappingMatcher<SimplePropDescriptor> m104 = new CompoundMappingMatcher<>(p104, g104.build());
        matchers.add(m104);

        // Version
        Pattern p105 = Pattern.compile(release + version, Pattern.CASE_INSENSITIVE);
        ImmutableMap.Builder<Integer, GroupEntry<SimplePropDescriptor>> g105 = ImmutableMap.builder();
        g105.put(0, subRlsNameEntry);
        g105.put(1, sceneMatcherEntry);
        g105.put(2, subRlsVersionEntry);
        MappingMatcher<SimplePropDescriptor> m105 = new CompoundMappingMatcher<>(p105, g105.build());
        matchers.add(m105);

        return new MultiMappingMatcher<>(matchers.build());
    }

    public static Site getSite() {
        return SITE;
    }

    public static ParsingService getParsingService() {
        return PARSING_SERVICE;
    }

    public static List<ParserEntry<?>> getParserEntries() {
        return PARSING_SERVICE.getEntries();
    }

    public static void registerSubtitleLanguageCorrectors(TypeBasedCorrectionService service) {
        service.registerCorrector(Subtitle.class, new SubtitleLanguageCorrector(new PatternStringReplacer(Pattern.compile("(en|eng|english)", Pattern.CASE_INSENSITIVE), "VO")));
        service.registerCorrector(Subtitle.class, new SubtitleLanguageCorrector(new PatternStringReplacer(Pattern.compile("(ger|german|deu|deutsch)", Pattern.CASE_INSENSITIVE), "de")));
    }
}

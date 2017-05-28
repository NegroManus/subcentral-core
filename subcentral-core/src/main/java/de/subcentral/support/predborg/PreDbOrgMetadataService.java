package de.subcentral.support.predborg;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.subcentral.core.metadata.Site;
import de.subcentral.core.metadata.release.Group;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.service.HttpMetadataService;
import de.subcentral.core.util.NetUtil;
import de.subcentral.core.util.TimeUtil;

/**
 * @implSpec #immutable #thread-safe
 */
public class PreDbOrgMetadataService extends HttpMetadataService {
    private static final Logger log       = LogManager.getLogger(PreDbOrgMetadataService.class);

    /**
     * The release dates are in UTC.
     */
    private static final ZoneId TIME_ZONE = ZoneId.of("UTC");

    PreDbOrgMetadataService() {
        // package-protected
    }

    @Override
    public Site getSite() {
        return PreDbOrg.getSite();
    }

    @Override
    public Set<Class<?>> getSupportedRecordTypes() {
        return ImmutableSet.of(Release.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> search(String query, Class<T> recordType) throws UnsupportedOperationException, IOException {
        if (recordType.isAssignableFrom(Release.class)) {
            URL url = buildRelativeUrl(buildSearchUrl(query));
            log.debug("Searching for releases with text query \"{}\" using url {}", query, url);
            return (List<T>) parseReleaseSearchResults(getDocument(url));
        }
        throw createRecordTypeNotSearchableException(recordType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String id, Class<T> recordType) throws UnsupportedOperationException, IOException {
        if (recordType.isAssignableFrom(Release.class)) {
            URL url = buildRelativeUrl(buildPostUrl(id));
            log.debug("Getting release with id {} using url {}", id, url);
            return (T) parseReleaseRecord(getDocument(url));
        }
        throw createUnsupportedRecordTypeException(recordType);
    }

    /**
     * {@code https://predb.org/search/game+of+thrones+s05e01/all}
     * 
     * @param query
     * @return
     */
    private static String buildSearchUrl(String query) {
        return String.format("/search/%s/all", NetUtil.encodeUrlFormValue(query));
    }

    /**
     * {@code https://predb.org/post/5726952}
     * 
     * @param id
     * @return
     */
    private static String buildPostUrl(String id) {
        return String.format("/post/%s", id);
    }

    /**
     * <pre>
     * <table class="table">
     *  <thead>
     *      <tr>
     *          <th>Ago</th>
     *          <th>Section</th>
     *          <th>Group</th>
     *          <th>Release</th>
     *      </tr>
     *  </thead>
     *
     *  <tbody>
     *      <tr class="post" id="5726952">
     *          ...
     *      </tr>
     *  </tbody>
     * </table>
     * </pre>
     * 
     * @param doc
     * @return
     * @throws IOException
     * @throws MalformedURLException
     */
    protected List<Release> parseReleaseSearchResults(Document doc) {
        Elements rlsTrs = doc.getElementsByClass("post");
        ImmutableList.Builder<Release> results = ImmutableList.builder();
        for (Element rlsTr : rlsTrs) {
            Release rls = parseReleaseSearchResult(rlsTr);
            results.add(rls);
        }
        return results.build();
    }

    /**
     * Normal:
     * 
     * <pre>
     * <tr class="post" id="5726952">
     *  <td class="pretime" id="time" data="1457883965" data-livestamp="1457883965">440d 20h 34m 33s</td>
     *  <td class="cat"><a href="/cats/TV-XVID" title="TV-XVID" alt="TV-XVID"><font color="#00FF00">TV-XVID</font></td>
     *  <td class="grp"><a href="/group/FLAME" title="FLAME" alt="FLAME">FLAME</td>
     *  <td class="rls"><a href="/post/5726952" title="Game.of.Thrones.S05E01.PL.BDRip.x264-FLAME" alt="Game.of.Thrones.S05E01.PL.BDRip.x264-FLAME">Game.of.Thrones.S05E01.PL.BDRip.x264-FLAME</a></td>
     * </tr>
     * </pre>
     * 
     * 
     * @param rlsDiv
     * @return
     * @throws IOException
     * @throws MalformedURLException
     */
    private Release parseReleaseSearchResult(Element rlsTr) {
        Release rls = new Release();

        String id = parseId(rlsTr);
        rls.setId(PreDbOrg.getSite(), id);

        Element preTimeTd = rlsTr.select("td[class*=pretime]").first();
        rls.setDate(parseReleaseDate(preTimeTd));

        Element catTd = rlsTr.select("td[class*=cat]").first();
        rls.setCategory(parseReleaseCategory(catTd));

        Element grpTd = rlsTr.select("td[class*=grp]").first();
        rls.setGroup(parseReleaseGroup(grpTd));

        Element rlsTd = rlsTr.select("td[class*=rls]").first();
        rls.setName(parseReleaseName(rlsTd));
        String postUrl = parsePostUrl(rlsTd);
        if (postUrl != null) {
            rls.getFurtherInfoLinks().add(postUrl);
        }

        return rls;
    }

    /**
     * Parses a release record.
     * 
     * @param doc
     * @param rls
     * @return
     * @throws IOException
     */
    protected Release parseReleaseRecord(Document doc) {
        throw new UnsupportedOperationException();
    }

    private static String parseId(Element rlsTr) {
        return rlsTr.attr("id");
    }

    private static ZonedDateTime parseReleaseDate(Element preTimeTd) {
        if (preTimeTd != null) {
            return TimeUtil.parseZonedDateTimeFromEpochSeond(preTimeTd.attr("data"), TIME_ZONE);
        }
        return null;
    }

    private static String parseReleaseCategory(Element catTd) {
        Element catAnchor = catTd.getElementsByTag("a").first();
        return parseCategory(catAnchor);
    }

    private static String parseCategory(Element categoryAnchor) {
        if (categoryAnchor != null) {
            return categoryAnchor.text();
        }
        return null;
    }

    private static Group parseReleaseGroup(Element grpTd) {
        if (grpTd != null) {
            return Group.ofOrNull(grpTd.text());
        }
        return null;
    }

    private static String parseReleaseName(Element rlsTd) {
        if (rlsTd != null) {
            return rlsTd.text();
        }
        return null;
    }

    private static String parsePostUrl(Element rlsTd) {
        if (rlsTd != null) {
            Element rlsAnchor = rlsTd.getElementsByTag("a").first();
            if (rlsAnchor != null) {
                return rlsAnchor.absUrl("href");
            }
        }
        return null;
    }
}

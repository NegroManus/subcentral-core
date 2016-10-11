package de.subcentral.mig.parse;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.collect.ImmutableList;

import de.subcentral.mig.Migration;
import de.subcentral.mig.ScContributor;
import de.subcentral.support.subcentralde.SubCentralDe;
import de.subcentral.support.woltlab.WoltlabBurningBoard.WbbPost;

public class SubberListParser {
    private static final Logger log = LogManager.getLogger(SubberListParser.class);
    private static final String URL = "http://subcentral.de/index.php?page=Thread&postID=33900";

    public SubberListData getAndParse() throws IOException {
        return parseThreadPage(Jsoup.parse(new URL(URL), Migration.TIMEOUT_MILLIS));
    }

    public SubberListData parseThreadPage(Document thread) {
        return parsePost(thread.outerHtml());
    }

    public SubberListData parsePost(WbbPost post) {
        return parsePost(post.getMessage());
    }

    public SubberListData parsePost(String postMessage) {
        Document doc = Jsoup.parse(postMessage, SubCentralDe.getSite().getLink());

        final Pattern userIdPattern = Pattern.compile("page=User&userID=(\\d+)");
        final SortedSet<ScContributor> subberList = new TreeSet<>();

        Element table = doc.getElementById("sptable");

        /**
         * <td><a href="http://www.subcentral.de/index.php?page=User&userID=21359" title= "Benutzerprofil von &raquo; **butterfly**&laquo; aufrufen" >**butterfly**</a></td>
         */
        Elements userAnchors = table.select("a[href*=page=User&userID=]");
        for (Element a : userAnchors) {
            String name = a.text().replace(" (PROBIE)", "");
            if (name.isEmpty()) {
                log.warn("Empty user name: {}", a);
                continue;
            }
            ScContributor subber = new ScContributor(ScContributor.Type.SUBBER);
            subber.setName(name);
            int id = 0;
            Matcher userIdMatcher = userIdPattern.matcher(a.attr("href"));
            if (userIdMatcher.find()) {
                id = Integer.parseInt(userIdMatcher.group(1));
            }
            subber.setId(id);
            subberList.add(subber);
        }

        return new SubberListData(subberList);
    }

    public static class SubberListData {
        private final List<ScContributor> subbers;

        public SubberListData(Iterable<ScContributor> subbers) {
            this.subbers = ImmutableList.copyOf(subbers);
        }

        public List<ScContributor> getSubbers() {
            return subbers;
        }
    }
}

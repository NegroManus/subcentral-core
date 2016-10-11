package de.subcentral.support.orlydbcom;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.common.io.Resources;

import de.subcentral.core.metadata.release.Release;

public class OrlyDbPlayground {
    public static void main(String[] args) throws IOException {
        OrlyDbComMetadataService db = new OrlyDbComMetadataService();

        URL url = Resources.getResource("de/subcentral/support/orlydbcom/psych.s06e05.html");
        Document doc = Jsoup.parse(url.openStream(), "UTF-8", db.getHost());

        List<Release> rlss = db.parseReleaseSearchResults(doc);
        rlss.stream().forEach((Release r) -> System.out.println(r));
    }
}

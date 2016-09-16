package de.subcentral.support.xrelto;

import java.net.URL;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.common.io.Resources;

import de.subcentral.core.metadata.release.Release;

public class XRelPlayground {

	public static void main(String[] args) throws Exception {
		XRelToMetadataService db = new XRelToMetadataService();

		URL url = Resources.getResource("de/subcentral/support/xrelto/psych.s05e06.html");
		Document doc = Jsoup.parse(url.openStream(), "UTF-8", db.getHost());

		List<Release> rlss = db.parseReleaseSearchResults(doc);
		rlss.stream().forEach((Release r) -> System.out.println(r));

	}
}

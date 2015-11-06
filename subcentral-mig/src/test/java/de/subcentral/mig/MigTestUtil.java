package de.subcentral.mig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.common.io.Resources;

public class MigTestUtil
{
	static Document parseDoc(Class<?> testClass, String filename) throws IOException
	{
		return Jsoup.parse(Resources.getResource(testClass, filename).openStream(), StandardCharsets.UTF_8.name(), "http://subcentral.de");
	}
}

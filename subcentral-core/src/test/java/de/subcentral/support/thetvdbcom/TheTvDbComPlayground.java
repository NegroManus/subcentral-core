package de.subcentral.support.thetvdbcom;

import java.io.IOException;
import java.util.List;

import de.subcentral.core.metadata.media.Series;

public class TheTvDbComPlayground
{
	/**
	 * -Dhttp.proxyHost=10.151.249.76 -Dhttp.proxyPort=8080
	 * 
	 * @param args
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	public static void main(String[] args) throws IllegalArgumentException, IOException
	{
		// A3ACA9D28A27792D

		TheTvDbMetadataDb db = new TheTvDbMetadataDb();
		db.setApiKey("A3ACA9D28A27792D");
		List<? extends Object> results = db.searchByObject(new Series("Psych"));
		results.stream().forEach((Object obj) -> System.out.println(obj));
	}
}

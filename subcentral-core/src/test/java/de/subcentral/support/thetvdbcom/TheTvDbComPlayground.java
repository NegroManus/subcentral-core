package de.subcentral.support.thetvdbcom;

import java.io.IOException;
import java.util.List;

import de.subcentral.core.metadata.media.Series;

public class TheTvDbComPlayground
{
	public static void main(String[] args) throws IllegalArgumentException, IOException
	{
		// A3ACA9D28A27792D

		TheTvDbMediaDb db = new TheTvDbMediaDb();
		db.setApiKey("A3ACA9D28A27792D");
		List<Series> results = db.search("Psych", Series.class);
		results.stream().forEach((Object obj) -> System.out.println(obj));
	}
}

package de.subcentral.core.lookup;

import java.net.MalformedURLException;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.impl.com.orlydb.OrlyDbLookup;
import de.subcentral.core.impl.com.orlydb.OrlyDbLookupResult;
import de.subcentral.core.impl.com.orlydb.OrlyDbQuery;
import de.subcentral.core.media.Media;
import de.subcentral.core.media.Medias;
import de.subcentral.core.naming.NamingStandards;
import de.subcentral.core.release.Group;
import de.subcentral.core.release.MediaRelease;
import de.subcentral.core.release.Releases;
import de.subcentral.core.release.Tag;

public class LookupTest
{
	public static void main(String[] args) throws LookupException, MalformedURLException
	{
		System.getProperties().put("http.proxyHost", "10.206.247.65");
		System.getProperties().put("http.proxyPort", "8080");

		Media media = Medias.newEpisode("Psych", 6, 5);

		MediaRelease rls = Releases.newMediaRelease(media, new Group("IMMERSE"), ImmutableList.of(new Tag("720p"), new Tag("HDTV"), new Tag("x264")));

		OrlyDbLookup lookup = new OrlyDbLookup();
		lookup.setNamingService(NamingStandards.NAMING_SERVICE);
		OrlyDbQuery query = lookup.createQuery(rls);
		OrlyDbLookupResult result = lookup.lookup(query);

		System.out.println("Results for: " + result.getUrl());
		for (MediaRelease foundRls : result.getAllResults())
		{
			System.out.println(foundRls.getName());
		}
	}
}

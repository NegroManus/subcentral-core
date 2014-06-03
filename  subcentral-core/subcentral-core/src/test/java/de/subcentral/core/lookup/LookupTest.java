package de.subcentral.core.lookup;

import java.net.MalformedURLException;

import de.subcentral.core.impl.com.orlydb.OrlyDbLookup;
import de.subcentral.core.impl.com.orlydb.OrlyDbLookupResult;
import de.subcentral.core.impl.com.orlydb.OrlyDbQuery;
import de.subcentral.core.impl.scene.Scene;
import de.subcentral.core.media.Media;
import de.subcentral.core.media.Medias;
import de.subcentral.core.release.MediaRelease;

public class LookupTest
{
	public static void main(String[] args) throws LookupException, MalformedURLException
	{
		System.getProperties().put("http.proxyHost", "10.206.247.65");
		System.getProperties().put("http.proxyPort", "8080");

		OrlyDbLookup lookup = new OrlyDbLookup();
		lookup.setNamingService(Scene.getSceneNamingService());
		OrlyDbQuery query = lookup.createQuery(Medias.newEpisode("Psych", Media.UNNUMBERED, Media.UNNUMBERED));
		OrlyDbLookupResult result = lookup.lookup(query);

		System.out.println("Results for: " + result.getUrl());
		for (MediaRelease rls : result.getAllResults())
		{
			System.out.println(rls.getName());
		}
	}
}

package de.subcentral.core.impl.com.orlydb;

import de.subcentral.core.media.Media;
import de.subcentral.core.release.MediaRelease;

public class OrlyDbQuery
{
	private String	section;
	private String	query;

	public String getSection()
	{
		return section;
	}

	public void setSection(String section)
	{
		this.section = section;
	}

	public String getQuery()
	{
		return query;
	}

	public void setQuery(String query)
	{
		this.query = query;
	}

	public static String buildQuery(Media media)
	{
		return media.getName().replace(' ', '.');
	}

	public static String buildQuery(MediaRelease release)
	{
		return release.getName();
	}
}

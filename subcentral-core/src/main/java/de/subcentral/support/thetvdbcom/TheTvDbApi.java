package de.subcentral.support.thetvdbcom;

import java.io.IOException;
import java.util.List;

import de.subcentral.core.metadata.media.Series;

public interface TheTvDbApi
{
	/**
	 * 
	 * @param name
	 *            the series' name
	 * @return
	 */
	public List<Series> getSeries(String name) throws IOException;
}

package de.subcentral.core.media;

import java.util.Set;

public interface AvMedia extends Media
{
	// Properties
	public String getOriginalLanguage();

	public Set<String> getCountriesOfOrigin();

	public int getRunningTime();
}

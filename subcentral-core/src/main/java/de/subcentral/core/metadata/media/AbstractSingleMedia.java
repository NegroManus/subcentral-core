package de.subcentral.core.metadata.media;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * For any media type that has no own class, like a movie, or if the media type could not be determined.
 *
 */
public abstract class AbstractSingleMedia extends AbstractNamedMedia
{
	protected final List<String>	languages	= new ArrayList<>(1);
	protected final List<String>	countries	= new ArrayList<>(1);
	protected final Set<String>		genres		= new HashSet<>(3);
	protected int					runningTime	= 0;

	public AbstractSingleMedia()
	{

	}

	public AbstractSingleMedia(String name)
	{
		this.name = name;
	}

	@Override
	public List<String> getLanguages()
	{
		return languages;
	}

	public void setLanguages(List<String> originalLanguages)
	{
		this.languages.clear();
		this.languages.addAll(originalLanguages);
	}

	@Override
	public List<String> getCountries()
	{
		return countries;
	}

	public void setCountries(List<String> countriesOfOrigin)
	{
		this.countries.clear();
		this.countries.addAll(countriesOfOrigin);
	}

	@Override
	public Set<String> getGenres()
	{
		return genres;
	}

	public void setGenres(Set<String> genres)
	{
		this.genres.clear();
		this.genres.addAll(genres);
	}

	@Override
	public int getRunningTime()
	{
		return runningTime;
	}

	public void setRunningTime(int runningTime)
	{
		this.runningTime = runningTime;
	}
}

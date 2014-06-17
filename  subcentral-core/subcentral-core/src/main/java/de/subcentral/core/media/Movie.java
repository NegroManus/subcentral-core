package de.subcentral.core.media;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Objects;

import de.subcentral.core.util.Settings;

public class Movie extends AbstractAvMedia implements Comparable<Movie>
{
	private String		name;
	private String		originalLanguage;
	private Set<String>	countriesOfOrigin	= new HashSet<>(1);

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public String getOriginalLanguage()
	{
		return originalLanguage;
	}

	public void setOriginalLanguage(String originalLanguage)
	{
		this.originalLanguage = originalLanguage;
	}

	@Override
	public Set<String> getCountriesOfOrigin()
	{
		return countriesOfOrigin;
	}

	public void setCountriesOfOrigin(Set<String> countriesOfOrigin)
	{
		this.countriesOfOrigin = countriesOfOrigin;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		if (this == obj)
		{
			return true;
		}
		if (Movie.class != obj.getClass())
		{
			return false;
		}
		Movie o = (Movie) obj;
		return Objects.equal(name, o.name);
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 23).append(name).toHashCode();
	}

	@Override
	public int compareTo(Movie o)
	{
		if (o == null)
		{
			return -1;
		}
		return Settings.STRING_ORDERING.compare(name, o.name);
	}

	@Override
	public String toString()
	{
		return Objects.toStringHelper(this)
				.omitNullValues()
				.add("name", name)
				.add("title", title)
				.add("date", date)
				.add("originalLanguage", originalLanguage)
				.add("countriesOfOrigin", countriesOfOrigin)
				.add("runningTime", runningTime)
				.add("genres", genres)
				.add("description", description)
				.add("coverUrl", coverUrl)
				.add("contentRating", contentRating)
				.add("contributions", contributions)
				.toString();
	}
}

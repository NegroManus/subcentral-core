package de.subcentral.core.media;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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
	public String computeName()
	{
		return name;
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
		return new EqualsBuilder().append(name, o.name).isEquals();
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
			return 1;
		}
		return new CompareToBuilder().append(name, o.name).toComparison();
	}
}

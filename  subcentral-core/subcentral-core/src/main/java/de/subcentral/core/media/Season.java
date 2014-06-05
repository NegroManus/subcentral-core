package de.subcentral.core.media;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import de.subcentral.core.naming.Nameable;

public class Season implements Comparable<Season>, Nameable
{
	private final Series	series;
	private int				number	= Media.UNNUMBERED;
	private String			title;
	private boolean			special;
	private String			description;
	private String			coverUrl;

	Season(Series series)
	{
		this.series = series;
	}

	public Series getSeries()
	{
		return series;
	}

	@Override
	public String getName()
	{
		return computeName();
	}

	@Override
	public String computeName()
	{
		return isNumbered() ? Integer.toString(number) : title;
	}

	public int getNumber()
	{
		return number;
	}

	public void setNumber(int number)
	{
		this.number = number;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public boolean isSpecial()
	{
		return special;
	}

	public void setSpecial(boolean special)
	{
		this.special = special;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getCoverUrl()
	{
		return coverUrl;
	}

	public void setCoverUrl(String coverUrl)
	{
		this.coverUrl = coverUrl;
	}

	// Convenience / Complex
	public boolean isNumbered()
	{
		return number != Media.UNNUMBERED;
	}

	public boolean isTitled()
	{
		return title != null;
	}

	public Episode addEpisode()
	{
		return series.addEpisode(this);
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
		if (Season.class != obj.getClass())
		{
			return false;
		}
		Season o = (Season) obj;
		return new EqualsBuilder().append(series, o.series).append(number, o.number).append(title, o.title).isEquals();
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(5, 13).append(series).append(number).append(title).toHashCode();
	}

	@Override
	public int compareTo(Season o)
	{
		if (o == null)
		{
			return 1;
		}
		return new CompareToBuilder().append(series, o.series).append(number, o.number).append(title, o.title).toComparison();
	}
}

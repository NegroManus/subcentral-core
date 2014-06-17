package de.subcentral.core.subtitle;

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;

import de.subcentral.core.contribution.Contribution;
import de.subcentral.core.contribution.Work;
import de.subcentral.core.media.AvMedia;
import de.subcentral.core.util.Settings;

public class Subtitle implements Work, Comparable<Subtitle>
{
	public static final String	CONTRIBUTION_TYPE_TRANSCRIPT	= "TRANSCRIPT";
	public static final String	CONTRIBUTION_TYPE_TIMINGS		= "TIMINGS";
	public static final String	CONTRIBUTION_TYPE_TRANSLATION	= "TRANSLATION";
	public static final String	CONTRIBUTION_TYPE_REVISION		= "REVISION";

	private AvMedia				media;
	private String				language;
	private Temporal			date;
	private String				productionType;
	private String				description;
	private List<Contribution>	contributions					= new ArrayList<>();

	public AvMedia getMedia()
	{
		return media;
	}

	public void setMedia(AvMedia media)
	{
		this.media = media;
	}

	public String getLanguage()
	{
		return language;
	}

	public void setLanguage(String language)
	{
		this.language = language;
	}

	public Temporal getDate()
	{
		return date;
	}

	public void setDate(Temporal date)
	{
		this.date = date;
	}

	public String getProductionType()
	{
		return productionType;
	}

	public void setProductionType(String productionType)
	{
		this.productionType = productionType;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public boolean isTranslation()
	{
		return !language.equals(media.getOriginalLanguage());
	}

	@Override
	public List<Contribution> getContributions()
	{
		return contributions;
	}

	public void setContributions(List<Contribution> contributions)
	{
		this.contributions = contributions;
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
		if (Subtitle.class != obj.getClass())
		{
			return false;
		}
		Subtitle o = (Subtitle) obj;
		return new EqualsBuilder().append(media, o.media).append(language, o.language).append(date, o.date).isEquals();
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(37, 99).append(media).append(language).append(date).toHashCode();
	}

	@Override
	public int compareTo(Subtitle o)
	{
		if (o == null)
		{
			return -1;
		}
		return ComparisonChain.start()
				.compare(media.getName(), o.media.getName(), Settings.STRING_ORDERING)
				.compare(language, o.language)
				.compare(date, o.date, Settings.TEMPORAL_ORDERING)
				.result();
	}

	@Override
	public String toString()
	{
		return Objects.toStringHelper(this)
				.omitNullValues()
				.add("media", media)
				.add("language", language)
				.add("date", date)
				.add("productionType", productionType)
				.add("description", description)
				.add("contributions", contributions)
				.toString();
	}
}

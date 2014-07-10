package de.subcentral.core.model.subtitle;

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;

import de.subcentral.core.model.Contribution;
import de.subcentral.core.model.Work;
import de.subcentral.core.model.media.AvMediaItem;
import de.subcentral.core.model.media.Medias;
import de.subcentral.core.util.Settings;

public class Subtitle implements Work, Comparable<Subtitle>
{
	public static final String	CONTRIBUTION_TYPE_TRANSCRIPT	= "TRANSCRIPT";
	public static final String	CONTRIBUTION_TYPE_TIMINGS		= "TIMINGS";
	public static final String	CONTRIBUTION_TYPE_TRANSLATION	= "TRANSLATION";
	public static final String	CONTRIBUTION_TYPE_REVISION		= "REVISION";

	private AvMediaItem			mediaItem;
	private String				language;
	private Temporal			date;
	private String				productionType;
	private String				description;
	private List<Contribution>	contributions					= new ArrayList<>();

	public AvMediaItem getMediaItem()
	{
		return mediaItem;
	}

	public void setMediaItem(AvMediaItem mediaItem)
	{
		this.mediaItem = mediaItem;
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
		return !language.equals(mediaItem.getOriginalLanguage());
	}

	@Override
	public List<Contribution> getContributions()
	{
		return contributions;
	}

	public void setContributions(List<Contribution> contributions)
	{
		Validate.notNull(contributions, "contributions cannot be null");
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
		return new EqualsBuilder().append(mediaItem, o.mediaItem).append(language, o.language).append(date, o.date).isEquals();
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(37, 99).append(mediaItem).append(language).append(date).toHashCode();
	}

	@Override
	public int compareTo(Subtitle o)
	{
		if (o == null)
		{
			return -1;
		}
		return ComparisonChain.start()
				.compare(mediaItem, o.mediaItem, Medias.MEDIA_NAME_COMPARATOR)
				.compare(language, o.language)
				.compare(date, o.date, Settings.TEMPORAL_ORDERING)
				.result();
	}

	@Override
	public String toString()
	{
		return Objects.toStringHelper(this)
				.omitNullValues()
				.add("mediaItem", mediaItem)
				.add("language", language)
				.add("date", date)
				.add("productionType", productionType)
				.add("description", description)
				.add("contributions", contributions)
				.toString();
	}
}

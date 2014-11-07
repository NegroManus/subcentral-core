package de.subcentral.core.model.media;

import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ComparisonChain;

import de.subcentral.core.Settings;

public class Contribution implements Comparable<Contribution>
{
	private Contributor	contributor;
	private String		type;
	private String		description;
	private int			amount		= 0;
	private float		progress	= 1.0f;

	public Contribution()
	{

	}

	public Contribution(Contributor contributor, String type, String description)
	{
		this.contributor = contributor;
		this.type = type;
		this.description = description;
	}

	public Contribution(Contributor contributor, String type, String description, int amount, float progress)
	{
		this.contributor = contributor;
		this.type = type;
		this.description = description;
		setAmount(amount);
		setProgress(progress);
	}

	/**
	 * The person / company / etc that contributed.
	 * 
	 * @return the contributor
	 */
	public Contributor getContributor()
	{
		return contributor;
	}

	public void setContributor(Contributor contributor)
	{
		this.contributor = contributor;
	}

	/**
	 * The type of the contribution.
	 * 
	 * @return the contribution type
	 */
	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	/**
	 * The amount of the contribution. This is a relative value. How big that amount is, can only be determined by knowing the amount of the other
	 * contributions. The default value is 0L (not measurable).
	 * 
	 * @return the amount (a zero or positive int)
	 */
	public int getAmount()
	{
		return amount;
	}

	public void setAmount(int amount) throws IllegalArgumentException
	{
		if (amount < 0)
		{
			throw new IllegalArgumentException("amount must be zero or positive");
		}
		this.amount = amount;
	}

	/**
	 * The progress of the contribution. A percentage value between 0.0f and 1.0f inclusively. The default value is 1.0f (complete).
	 * 
	 * @return the progress (0.0f - 1.0f)
	 */
	public float getProgress()
	{
		return progress;
	}

	public void setProgress(float progress) throws IllegalArgumentException
	{
		if (progress < 0.0f || progress > 1.0f)
		{
			throw new IllegalArgumentException("progress must be inclusively between 0.0 and 1.0");
		}
		this.progress = progress;
	}

	/**
	 * Further description / specification of the contribution.
	 * 
	 * @return the description
	 */
	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	// Object methods
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj instanceof Contribution)
		{
			Contribution o = (Contribution) obj;
			return Objects.equals(contributor, o.contributor) && Objects.equals(type, o.type) && Objects.equals(description, o.description);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(63, 19).append(contributor).append(type).append(description).toHashCode();
	}

	@Override
	public int compareTo(Contribution o)
	{
		// nulls first
		if (o == null)
		{
			return 1;
		}
		return ComparisonChain.start()
				.compare(contributor != null ? contributor.getName() : null,
						o.contributor != null ? o.contributor.getName() : null,
						Settings.STRING_ORDERING)
				.compare(type, o.type, Settings.STRING_ORDERING)
				.compare(description, o.description, Settings.STRING_ORDERING)
				.result();
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(Contribution.class)
				.omitNullValues()
				.add("contributor", contributor)
				.add("type", type)
				.add("amount", amount)
				.add("progress", progress)
				.add("description", description)
				.toString();
	}
}

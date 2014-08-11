package de.subcentral.core.model;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ComparisonChain;

import de.subcentral.core.Settings;

public class Contribution implements Comparable<Contribution>
{
	private String		type;
	private Contributor	contributor;
	private long		amount		= 0L;
	private double		progress	= 1.0d;
	private String		description;

	public Contribution()
	{

	}

	public Contribution(String type, Contributor contributor, long amount, double progress, String description)
	{
		this.type = type;
		this.contributor = contributor;
		this.amount = amount;
		this.progress = progress;
		this.description = description;
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
	 * The amount of the contribution. This is a relative value. How big that amount is, can only be determined when knowing the other contributions.
	 * The default value is 0L (not measurable).
	 * 
	 * @return the amount (a zero or positive long)
	 */
	public long getAmount()
	{
		return amount;
	}

	public void setAmount(long amount)
	{
		Validate.inclusiveBetween(0L, Long.MAX_VALUE, amount);
		this.amount = amount;
	}

	/**
	 * The progress of the contribution. A percentage value between 0.0d and 1.0d inclusively. The default value is 1.0d (complete).
	 * 
	 * @return the progress (0.0d - 1.0d)
	 */
	public double getProgress()
	{
		return progress;
	}

	public void setProgress(double progress)
	{
		Validate.inclusiveBetween(0d, 1d, progress);
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
		if (obj != null && Contribution.class.equals(obj.getClass()))
		{
			Contribution o = (Contribution) obj;
			return new EqualsBuilder().append(type, o.type).append(contributor, o.contributor).append(description, o.description).isEquals();
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(63, 19).append(type).append(contributor).append(description).toHashCode();
	}

	@Override
	public int compareTo(Contribution o)
	{
		if (o == null)
		{
			return -1;
		}
		return ComparisonChain.start()
				.compare(type, o.type, Settings.STRING_ORDERING)
				.compare(contributor != null ? contributor.getName() : null,
						o.contributor != null ? o.contributor.getName() : null,
						Settings.STRING_ORDERING)
				.compare(description, o.description, Settings.STRING_ORDERING)
				.result();
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(Contribution.class)
				.omitNullValues()
				.add("type", type)
				.add("contributor", contributor)
				.add("amount", amount)
				.add("progress", progress)
				.add("description", description)
				.toString();
	}
}

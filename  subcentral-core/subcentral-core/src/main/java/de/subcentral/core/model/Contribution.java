package de.subcentral.core.model;

import org.apache.commons.lang3.Validate;

public class Contribution
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
	 * The default value is 0L.
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
	 * The progress of the contribution. A percentage value between 0.0d and 1.0d inclusively. The default value is 1.0d.
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
}

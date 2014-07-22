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
	 * 
	 * @return The type of the contribution.
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
	 * 
	 * @return The person / company / etc that contributed.
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
	 * 
	 * @return The amount of the contribution. This is a relative value. How big that amount is, can only be determined when knowing the other
	 *         contributions. The default value is 0L.
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
	 * 
	 * @return The progress of the contribution. A percentage value (0.0d - 1.0d). The default value is 1.0d.
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

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}
}

package de.subcentral.core.model;

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

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public Contributor getContributor()
	{
		return contributor;
	}

	public void setContributor(Contributor contributor)
	{
		this.contributor = contributor;
	}

	public long getAmount()
	{
		return amount;
	}

	public void setAmount(long amount)
	{
		this.amount = amount;
	}

	public double getProgress()
	{
		return progress;
	}

	public void setProgress(double progress)
	{
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

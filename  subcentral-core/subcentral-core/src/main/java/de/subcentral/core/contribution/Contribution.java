package de.subcentral.core.contribution;

public class Contribution
{
	private String	type;
	private long	amount		= 0L;
	private double	progress	= 1.0d;
	private String	description;

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
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

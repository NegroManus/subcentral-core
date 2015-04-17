package de.subcentral.core.subtitle;

public class Item
{
	private long	start;
	private long	end;
	private String	text;

	public long getStart()
	{
		return start;
	}

	public void setStart(long start)
	{
		if (start < 0)
		{
			throw new IllegalArgumentException("start cannot be negative");
		}
		this.start = start;
	}

	public long getEnd()
	{
		return end;
	}

	public void setEnd(long end)
	{
		if (start < 0)
		{
			throw new IllegalArgumentException("end cannot be negative");
		}
		this.end = end;
	}

	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	public long getDuration()
	{
		return end - start;
	}

	public void setDuration(long duration)
	{
		if (duration < 0)
		{
			throw new IllegalArgumentException("duration cannot be negative");
		}
		this.end = start + duration;
	}

}

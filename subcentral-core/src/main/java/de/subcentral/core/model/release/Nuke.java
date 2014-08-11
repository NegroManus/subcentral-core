package de.subcentral.core.model.release;

import java.time.temporal.Temporal;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ComparisonChain;

import de.subcentral.core.Settings;
import de.subcentral.core.model.Models;

public class Nuke implements Comparable<Nuke>
{
	private String		reason;
	private Temporal	date;

	public Nuke()
	{

	}

	public Nuke(String reason)
	{
		this.reason = reason;
	}

	public Nuke(String reason, Temporal date)
	{
		this.reason = reason;
		this.date = date;
	}

	public String getReason()
	{
		return reason;
	}

	public void setReason(String reason)
	{
		this.reason = reason;
	}

	public Temporal getDate()
	{
		return date;
	}

	public void setDate(Temporal date) throws IllegalArgumentException
	{
		Models.validateTemporalClass(date);
		this.date = date;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj != null && Nuke.class.equals(obj.getClass()))
		{
			Nuke o = (Nuke) obj;
			return new EqualsBuilder().append(reason, o.reason).append(date, o.date).isEquals();
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(9, 77).append(reason).append(date).toHashCode();
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(Nuke.class).omitNullValues().add("reason", reason).add("date", date).toString();
	}

	@Override
	public int compareTo(Nuke o)
	{
		// nulls last
		if (o == null)
		{
			return -1;
		}
		// sort by date, then reason
		return ComparisonChain.start().compare(date, o.date, Settings.TEMPORAL_ORDERING).compare(reason, o.reason, Settings.STRING_ORDERING).result();
	}
}

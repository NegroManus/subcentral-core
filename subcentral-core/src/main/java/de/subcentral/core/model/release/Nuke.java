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
	private final String	reason;
	private final Temporal	date;

	public Nuke(String reason)
	{
		this(reason, null);
	}

	public Nuke(String reason, Temporal date) throws IllegalArgumentException
	{
		this.reason = reason;
		this.date = Models.validateTemporalClass(date);
	}

	public String getReason()
	{
		return reason;
	}

	public Temporal getDate()
	{
		return date;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj != null && getClass().equals(obj.getClass()))
		{
			Nuke o = (Nuke) obj;
			return new EqualsBuilder().append(reason, o.reason).append(date, o.date).isEquals();
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(9, 71).append(reason).append(date).toHashCode();
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

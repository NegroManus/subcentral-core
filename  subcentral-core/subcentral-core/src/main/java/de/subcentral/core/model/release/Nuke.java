package de.subcentral.core.model.release;

import java.time.temporal.Temporal;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;

import de.subcentral.core.Settings;
import de.subcentral.core.model.Models;

public class Nuke implements Comparable<Nuke>
{
	private Temporal	date;
	private String		reason;

	public Temporal getDate()
	{
		return date;
	}

	public void setDate(Temporal date)
	{
		Models.validateDateClass(date);
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
			return new EqualsBuilder().append(date, o.date).append(reason, o.reason).isEquals();
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(9, 77).append(date).append(reason).toHashCode();
	}

	@Override
	public String toString()
	{
		return Objects.toStringHelper(this).omitNullValues().add("date", date).add("reason", reason).toString();
	}

	@Override
	public int compareTo(Nuke o)
	{
		// nulls last
		if (o == null)
		{
			return -1;
		}
		return ComparisonChain.start().compare(date, o.date, Settings.TEMPORAL_ORDERING).compare(reason, o.reason, Settings.STRING_ORDERING).result();
	}
}

package de.subcentral.core.metadata.release;

import java.io.Serializable;
import java.time.temporal.Temporal;
import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ComparisonChain;

import de.subcentral.core.BeanUtil;
import de.subcentral.core.Settings;
import de.subcentral.core.util.TemporalComparator;

public abstract class NukeBase implements Comparable<NukeBase>, Serializable
{
	private static final long	serialVersionUID	= 1349086787037911245L;

	private final String		reason;
	private final Temporal		date;

	public NukeBase(String reason, Temporal date) throws IllegalArgumentException
	{
		this.reason = reason;
		this.date = BeanUtil.validateTemporalClass(date);
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
		if (obj != null && getClass() == obj.getClass())
		{
			NukeBase o = (NukeBase) obj;
			return Objects.equals(reason, o.reason) && Objects.equals(date, o.date);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(9, 71).append(getClass()).append(reason).append(date).toHashCode();
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(this.getClass()).omitNullValues().add("reason", reason).add("date", date).toString();
	}

	@Override
	public int compareTo(NukeBase o)
	{
		// nulls first
		if (o == null)
		{
			return 1;
		}
		// sort by date, then reason
		return ComparisonChain.start().compare(date, o.date, TemporalComparator.INSTANCE).compare(reason, o.reason, Settings.STRING_ORDERING).result();
	}
}

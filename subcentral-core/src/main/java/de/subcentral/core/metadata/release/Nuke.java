package de.subcentral.core.metadata.release;

import java.io.Serializable;
import java.time.temporal.Temporal;
import java.util.Objects;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ComparisonChain;

import de.subcentral.core.util.ObjectUtil;
import de.subcentral.core.util.TemporalComparator;
import de.subcentral.core.util.ValidationUtil;

public class Nuke implements Comparable<Nuke>, Serializable
{
	private static final long	serialVersionUID	= 8172872894931232487L;

	private final String		reason;
	private final Temporal		date;
	private final boolean		unnuke;

	public Nuke(String reason)
	{
		this(reason, null, false);
	}

	public Nuke(String reason, Temporal date)
	{
		this(reason, date, false);
	}

	public Nuke(String reason, boolean unnuke)
	{
		this(reason, null, unnuke);
	}

	public Nuke(String reason, Temporal date, boolean unnuke) throws IllegalArgumentException
	{
		this.reason = reason;
		this.date = ValidationUtil.validateTemporalClass(date);
		this.unnuke = unnuke;
	}

	public String getReason()
	{
		return reason;
	}

	public Temporal getDate()
	{
		return date;
	}

	public boolean isUnnuke()
	{
		return unnuke;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj instanceof Nuke)
		{
			Nuke o = (Nuke) obj;
			return Objects.equals(reason, o.reason) && Objects.equals(date, o.date) && unnuke == o.unnuke;
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(Nuke.class, reason, date, unnuke);
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(this.getClass()).omitNullValues().add("reason", reason).add("date", date).add("unnuke", unnuke).toString();
	}

	@Override
	public int compareTo(Nuke o)
	{
		if (this == o)
		{
			return 0;
		}
		// nulls first
		if (o == null)
		{
			return 1;
		}
		// sort by date, then reason
		return ComparisonChain.start().compare(date, o.date, TemporalComparator.INSTANCE).compare(reason, o.reason, ObjectUtil.getDefaultStringOrdering()).compareFalseFirst(unnuke, o.unnuke).result();
	}
}

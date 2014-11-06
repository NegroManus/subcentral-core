package de.subcentral.core.model.release;

import java.time.temporal.Temporal;
import java.util.Locale;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ComparisonChain;

import de.subcentral.core.Settings;
import de.subcentral.core.model.Models;
import de.subcentral.core.util.TemporalComparator;

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
		if (obj instanceof Nuke)
		{
			Nuke o = (Nuke) obj;
			return StringUtils.equalsIgnoreCase(reason, o.reason) && Objects.equals(date, o.date);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(9, 71).append(StringUtils.lowerCase(reason, Locale.ENGLISH)).append(date).toHashCode();
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(Nuke.class).omitNullValues().add("reason", reason).add("date", date).toString();
	}

	@Override
	public int compareTo(Nuke o)
	{
		// nulls first
		if (o == null)
		{
			return 1;
		}
		// sort by date, then reason
		return ComparisonChain.start()
				.compare(date, o.date, TemporalComparator.INSTANCE)
				.compare(reason, o.reason, Settings.STRING_ORDERING)
				.result();
	}
}

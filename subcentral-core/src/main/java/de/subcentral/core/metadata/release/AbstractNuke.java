package de.subcentral.core.metadata.release;

import java.time.temporal.Temporal;
import java.util.Locale;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ComparisonChain;

import de.subcentral.core.BeanUtil;
import de.subcentral.core.Settings;
import de.subcentral.core.util.TemporalComparator;

public abstract class AbstractNuke implements Comparable<AbstractNuke>
{
	private final String	reason;
	private final Temporal	date;

	public AbstractNuke(String reason, Temporal date) throws IllegalArgumentException
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
		if (getClass() == obj.getClass())
		{
			AbstractNuke o = (AbstractNuke) obj;
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
		return MoreObjects.toStringHelper(this.getClass()).omitNullValues().add("reason", reason).add("date", date).toString();
	}

	@Override
	public int compareTo(AbstractNuke o)
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

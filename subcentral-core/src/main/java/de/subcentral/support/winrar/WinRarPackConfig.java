package de.subcentral.support.winrar;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.google.common.base.MoreObjects;

public class WinRarPackConfig
{
	public static enum CompressionMethod
	{
		STORE(0, "Store", "Add files to archive without compression"),
		FASTEST(1, "Fastest", "Fastest method (least compressive)"),
		FAST(2, "Fast", "Fast compression method"),
		NORMAL(3, "Normal", "Normal (default) compression method"),
		GOOD(4, "Good", "Good compression method (more compressive)"),
		BEST(5, "Best", "Best compression method (most compressive but also most slow");

		private final int		code;
		private final String	label;
		private final String	description;

		private CompressionMethod(int code, String label, String description)
		{
			this.code = code;
			this.label = label;
			this.description = description;
		}

		public final int getCode()
		{
			return code;
		}

		public String getLabel()
		{
			return label;
		}

		public final String getDescription()
		{
			return description;
		}

		public static final CompressionMethod of(int code) throws IllegalArgumentException
		{
			for (CompressionMethod m : values())
			{
				if (code == m.getCode())
				{
					return m;
				}
			}
			throw new IllegalArgumentException("Unknown code for compression method " + code);
		}
	}

	private boolean				replaceTarget		= true;
	private boolean				deleteSource		= false;
	private CompressionMethod	compressionMethod	= CompressionMethod.NORMAL;
	private long				timeout				= 15;
	private TimeUnit			timeoutUnit			= TimeUnit.SECONDS;

	public boolean getReplaceTarget()
	{
		return replaceTarget;
	}

	public void setReplaceTarget(boolean replaceTarget)
	{
		this.replaceTarget = replaceTarget;
	}

	public boolean getDeleteSource()
	{
		return deleteSource;
	}

	public void setDeleteSource(boolean deleteSource)
	{
		this.deleteSource = deleteSource;
	}

	public CompressionMethod getCompressionMethod()
	{
		return compressionMethod;
	}

	public void setCompressionMethod(CompressionMethod compressionMethod)
	{
		Objects.requireNonNull(compressionMethod);
		this.compressionMethod = compressionMethod;
	}

	public long getTimeout()
	{
		return timeout;
	}

	public void setTimeout(long timeout)
	{
		this.timeout = timeout;
	}

	public TimeUnit getTimeoutUnit()
	{
		return timeoutUnit;
	}

	public void setTimeoutUnit(TimeUnit timeoutUnit)
	{
		this.timeoutUnit = timeoutUnit;
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(WinRarPackConfig.class)
				.omitNullValues()
				.add("replaceTarget", replaceTarget)
				.add("deleteSource", deleteSource)
				.add("compressionMethod", compressionMethod)
				.add("timeout", timeout)
				.add("timeoutUnit", timeoutUnit)
				.toString();
	}
}

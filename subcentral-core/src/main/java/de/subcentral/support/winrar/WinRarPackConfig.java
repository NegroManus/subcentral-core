package de.subcentral.support.winrar;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.google.common.base.MoreObjects;

public class WinRarPackConfig
{
	public static enum DeletionMode
	{
		/**
		 * Keep files.
		 */
		KEEP,
		/**
		 * Delete files to Recycle Bin. Delete files after archiving and place them to Recycle Bin. Available in Windows version only.
		 */
		RECYCLE,

		/**
		 * Delete files after archiving. Move files to archive.
		 */
		DELETE;
	}

	public static enum CompressionMethod
	{
		/**
		 * Add files to archive without compression.
		 */
		STORE(0, "Store", "Add files to archive without compression"),
		/**
		 * Fastest method (least compressive).
		 */
		FASTEST(1, "Fastest", "Fastest method (least compressive)"),
		/**
		 * Fast compression method.
		 */
		FAST(2, "Fast", "Fast compression method"),
		/**
		 * Normal (default) compression method.
		 */
		NORMAL(3, "Normal", "Normal (default) compression method"),
		/**
		 * Good compression method (more compressive).
		 */
		GOOD(4, "Good", "Good compression method (more compressive)"),
		/**
		 * Best compression method (most compressive but also most slow")
		 */
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

	private boolean				overwriteTarget		= true;
	private DeletionMode		sourceDeletionMode	= DeletionMode.KEEP;
	private CompressionMethod	compressionMethod	= CompressionMethod.NORMAL;
	private long				timeoutValue		= 15;
	private TimeUnit			timeoutUnit			= TimeUnit.SECONDS;

	public boolean getOverwriteTarget()
	{
		return overwriteTarget;
	}

	public void setOverwriteTarget(boolean overwriteTarget)
	{
		this.overwriteTarget = overwriteTarget;
	}

	public DeletionMode getSourceDeletionMode()
	{
		return sourceDeletionMode;
	}

	public void setSourceDeletionMode(DeletionMode sourceDeletionMode)
	{
		this.sourceDeletionMode = Objects.requireNonNull(sourceDeletionMode);
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

	public long getTimeoutValue()
	{
		return timeoutValue;
	}

	public TimeUnit getTimeoutUnit()
	{
		return timeoutUnit;
	}

	public void setTimeout(long timeout, TimeUnit unit)
	{
		this.timeoutValue = timeout;
		this.timeoutUnit = unit;
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(WinRarPackConfig.class)
				.omitNullValues()
				.add("overwriteTarget", overwriteTarget)
				.add("sourceDeletionMode", sourceDeletionMode)
				.add("compressionMethod", compressionMethod)
				.add("timeout", timeoutValue)
				.add("timeoutUnit", timeoutUnit)
				.toString();
	}
}

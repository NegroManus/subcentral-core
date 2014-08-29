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
		 * Do not compress file when adding to archive.
		 */
		STORE(0, "Store", "Do not compress file when adding to archive."),
		/**
		 * Use fastest method (less compressive).
		 */
		FASTEST(1, "Fastest", "Use fastest method (less compressive)."),
		/**
		 * Use fast compression method.
		 */
		FAST(2, "Fast", "Use fast compression method."),
		/**
		 * Use normal (default) compression method.
		 */
		NORMAL(3, "Normal", "Use normal (default) compression method."),
		/**
		 * Use good compression method (more compressive, but slower).
		 */
		GOOD(4, "Good", "Use good compression method (more compressive, but slower)."),
		/**
		 * Use best compression method (slightly more compressive, but slowest).
		 */
		BEST(5, "Best", "Use best compression method (slightly more compressive, but slowest).");

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

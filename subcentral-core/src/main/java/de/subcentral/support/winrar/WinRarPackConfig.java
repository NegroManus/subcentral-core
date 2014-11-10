package de.subcentral.support.winrar;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.google.common.base.MoreObjects;

public class WinRarPackConfig
{
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

		/**
		 * The unique integer code.
		 * 
		 * @return the code
		 */
		public final int getCode()
		{
			return code;
		}

		/**
		 * The unique human readable label / name.
		 * 
		 * @return the label
		 */
		public String getLabel()
		{
			return label;
		}

		/**
		 * The description text.
		 * 
		 * @return the description
		 */
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

	public static enum OverwriteMode
	{
		/**
		 * Do not add already existing files to the archive.
		 */
		SKIP,

		/**
		 * Overwrite already existing files in the archive. Keep other files that may be in the target archive.
		 */
		UPDATE,

		/**
		 * If the target archive already exists, it is deleted first. So no old files will be in the new archive.
		 */
		REPLACE;
	}

	private CompressionMethod	compressionMethod	= CompressionMethod.NORMAL;
	private long				timeoutValue		= 15;
	private TimeUnit			timeoutUnit			= TimeUnit.SECONDS;
	private OverwriteMode		targetOverwriteMode	= OverwriteMode.REPLACE;
	private DeletionMode		sourceDeletionMode	= DeletionMode.KEEP;

	/**
	 * The default value is {@link CompressionMethod#NORMAL}.
	 * 
	 * @return the compression method
	 */
	public CompressionMethod getCompressionMethod()
	{
		return compressionMethod;
	}

	public void setCompressionMethod(CompressionMethod compressionMethod)
	{
		this.compressionMethod = Objects.requireNonNull(compressionMethod);
	}

	/**
	 * The default value is {@code 15}.
	 * 
	 * @return the timeout value
	 */
	public long getTimeoutValue()
	{
		return timeoutValue;
	}

	/**
	 * The default value is {@link TimeUnit#SECONDS}.
	 * 
	 * @return the timeout unit
	 */
	public TimeUnit getTimeoutUnit()
	{
		return timeoutUnit;
	}

	public void setTimeout(long timeout, TimeUnit unit)
	{
		this.timeoutValue = timeout;
		this.timeoutUnit = Objects.requireNonNull(unit, "unit");
	}

	/**
	 * The default value is {@link OverwriteMode#REPLACE}.
	 * 
	 * @return the overwrite mode for the target package
	 */
	public OverwriteMode getTargetOverwriteMode()
	{
		return targetOverwriteMode;
	}

	public void setTargetOverwriteMode(OverwriteMode targetOverwriteMode)
	{
		this.targetOverwriteMode = Objects.requireNonNull(targetOverwriteMode);
	}

	/**
	 * The default value is {@link DeletionMode#KEEP}.
	 * 
	 * @return the deletion mode for the source file
	 */
	public DeletionMode getSourceDeletionMode()
	{
		return sourceDeletionMode;
	}

	public void setSourceDeletionMode(DeletionMode sourceDeletionMode)
	{
		this.sourceDeletionMode = Objects.requireNonNull(sourceDeletionMode);
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(WinRarPackConfig.class)
				.omitNullValues()
				.add("compressionMethod", compressionMethod)
				.add("timeout", timeoutValue + " " + timeoutUnit)
				.add("targetOverwriteMode", targetOverwriteMode)
				.add("sourceDeletionMode", sourceDeletionMode)
				.toString();
	}
}

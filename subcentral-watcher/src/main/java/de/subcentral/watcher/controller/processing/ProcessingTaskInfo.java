package de.subcentral.watcher.controller.processing;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ProcessingTaskInfo implements ProcessingInfo
{
	public static enum Flag
	{
		DELETED_SOURCE_FILE
	};

	private final Set<Flag>	flags;
	private final Throwable	exception;

	private ProcessingTaskInfo(Set<Flag> flags, Throwable exception)
	{
		this.flags = Objects.requireNonNull(flags, "flags");
		this.exception = exception;
	}

	public Set<Flag> getFlags()
	{
		return flags;
	}

	public Throwable getException()
	{
		return exception;
	}

	public boolean failed()
	{
		return exception != null;
	}

	public static ProcessingTaskInfo withAdditonalFlags(ProcessingTaskInfo currentInfo, Flag... additionalFlags)
	{
		List<Flag> list = Arrays.asList(additionalFlags);
		if (currentInfo == null)
		{
			return new ProcessingTaskInfo(EnumSet.copyOf(list), null);
		}
		else
		{
			Set<Flag> flags = new HashSet<>(currentInfo.getFlags());
			flags.addAll(list);
			return new ProcessingTaskInfo(EnumSet.copyOf(flags), null);
		}
	}

	public static ProcessingTaskInfo failed(Throwable exception)
	{
		return new ProcessingTaskInfo(EnumSet.noneOf(Flag.class), exception);
	}
}

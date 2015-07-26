package de.subcentral.watcher.controller.processing;

public class ProcessingTaskInfo implements ProcessingInfo
{
	private final String info;

	private ProcessingTaskInfo(String info)
	{
		this.info = info;
	}

	public String getInfo()
	{
		return info;
	}

	public static ProcessingTaskInfo of(String info)
	{
		return new ProcessingTaskInfo(info);
	}
}

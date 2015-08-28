package de.subcentral.watcher.controller.processing;

import java.util.Objects;

import de.subcentral.watcher.controller.processing.ProcessingResult.ReleaseOriginInfo;

public class ProcessingResultInfo implements ProcessingInfo
{
	private final ProcessingResult	processingResult;
	private final ReleaseOriginInfo	originInfo;

	private ProcessingResultInfo(ProcessingResult processingResult, ReleaseOriginInfo originInfo)
	{
		this.processingResult = Objects.requireNonNull(processingResult, "processingResult");
		this.originInfo = Objects.requireNonNull(originInfo, "originInfo");
	}

	public ProcessingResult getProcessingResult()
	{
		return processingResult;
	}

	public ReleaseOriginInfo getOriginInfo()
	{
		return originInfo;
	}

	public static ProcessingResultInfo of(ProcessingResult processingResult, ReleaseOriginInfo methodInfo)
	{
		return new ProcessingResultInfo(processingResult, methodInfo);
	}
}

package de.subcentral.watcher.controller.processing;

import java.util.Objects;

import de.subcentral.watcher.controller.processing.ProcessingResult.MethodInfo;

public class ProcessingResultInfo implements ProcessingInfo
{
    private final ProcessingResult processingResult;
    private final MethodInfo	   methodInfo;

    private ProcessingResultInfo(ProcessingResult processingResult, MethodInfo methodInfo)
    {
	this.processingResult = Objects.requireNonNull(processingResult, "processingResult");
	this.methodInfo = Objects.requireNonNull(methodInfo, "methodInfo");
    }

    public ProcessingResult getProcessingResult()
    {
	return processingResult;
    }

    public MethodInfo getMethodInfo()
    {
	return methodInfo;
    }

    public static ProcessingResultInfo of(ProcessingResult processingResult, MethodInfo methodInfo)
    {
	return new ProcessingResultInfo(processingResult, methodInfo);
    }
}

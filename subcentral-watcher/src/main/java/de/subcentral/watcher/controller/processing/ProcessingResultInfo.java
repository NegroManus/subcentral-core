package de.subcentral.watcher.controller.processing;

import java.util.Objects;

import de.subcentral.core.metadata.release.CompatibilityService.CompatibilityInfo;
import de.subcentral.core.metadata.release.StandardRelease;

public class ProcessingResultInfo implements ProcessingInfo
{
	public static enum ResultType
	{
		LISTED, LISTED_COMPATIBLE, GUESSED, GUESSED_COMPATIBLE
	}

	private final ResultType		resultType;
	private final StandardRelease	standardRelease;
	private final CompatibilityInfo	compatibilityInfo;

	private ProcessingResultInfo(ResultType resultType, StandardRelease standardRelease, CompatibilityInfo compatibilityInfo)
	{
		this.resultType = Objects.requireNonNull(resultType, "resultType");
		this.standardRelease = standardRelease;
		this.compatibilityInfo = compatibilityInfo;

	}

	public ResultType getResultType()
	{
		return resultType;
	}

	public StandardRelease getStandardRelease()
	{
		return standardRelease;
	}

	public CompatibilityInfo getCompatibilityInfo()
	{
		return compatibilityInfo;
	}

	public static ProcessingResultInfo listed()
	{
		return new ProcessingResultInfo(ResultType.LISTED, null, null);
	}

	public static ProcessingResultInfo listedCompatible(CompatibilityInfo compatibilityInfo)
	{
		return new ProcessingResultInfo(ResultType.LISTED_COMPATIBLE, null, compatibilityInfo);
	}

	public static ProcessingResultInfo guessed(StandardRelease standardRelease)
	{
		return new ProcessingResultInfo(ResultType.GUESSED, standardRelease, null);
	}

	public static ProcessingResultInfo guessedCompatible(StandardRelease standardRelease, CompatibilityInfo compatibilityInfo)
	{
		return new ProcessingResultInfo(ResultType.GUESSED, standardRelease, compatibilityInfo);
	}
}

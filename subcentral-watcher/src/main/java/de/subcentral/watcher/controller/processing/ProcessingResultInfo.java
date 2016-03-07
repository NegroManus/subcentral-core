package de.subcentral.watcher.controller.processing;

import java.util.Objects;

import de.subcentral.core.metadata.release.CompatibilityService.CompatibilityInfo;
import de.subcentral.core.metadata.release.StandardRelease;

public class ProcessingResultInfo implements ProcessingInfo
{
	public enum SourceType
	{
		LISTED, GUESSED
	}

	public enum RelationType
	{
		MATCH, COMPATIBLE, MANUAL
	}

	private ProcessingResult		result;
	private final SourceType		sourceType;
	private final RelationType		relationType;
	private final StandardRelease	standardRelease;
	private final CompatibilityInfo	compatibilityInfo;

	private ProcessingResultInfo(SourceType sourceType, RelationType relationType, StandardRelease standardRelease, CompatibilityInfo compatibilityInfo)
	{
		this.sourceType = Objects.requireNonNull(sourceType, "sourceType");
		this.relationType = Objects.requireNonNull(relationType, "relationType");
		this.standardRelease = standardRelease;
		this.compatibilityInfo = compatibilityInfo;
	}

	// package private
	void setResult(ProcessingResult result)
	{
		this.result = result;
	}

	public ProcessingResult getResult()
	{
		return result;
	}

	public SourceType getSourceType()
	{
		return sourceType;
	}

	public RelationType getRelationType()
	{
		return relationType;
	}

	public StandardRelease getStandardRelease()
	{
		return standardRelease;
	}

	public CompatibilityInfo getCompatibilityInfo()
	{
		return compatibilityInfo;
	}

	public static ProcessingResultInfo listedMatching()
	{
		return new ProcessingResultInfo(SourceType.LISTED, RelationType.MATCH, null, null);
	}

	public static ProcessingResultInfo listedCompatible(CompatibilityInfo compatibilityInfo)
	{
		return new ProcessingResultInfo(SourceType.LISTED, RelationType.COMPATIBLE, null, Objects.requireNonNull(compatibilityInfo, "compatibilityInfo"));
	}

	public static ProcessingResultInfo listedManual()
	{
		return new ProcessingResultInfo(SourceType.LISTED, RelationType.MANUAL, null, null);
	}

	public static ProcessingResultInfo guessedMatching(StandardRelease standardRelease)
	{
		return new ProcessingResultInfo(SourceType.GUESSED, RelationType.MATCH, standardRelease, null);
	}

	public static ProcessingResultInfo guessedCompatible(StandardRelease standardRelease, CompatibilityInfo compatibilityInfo)
	{
		return new ProcessingResultInfo(SourceType.GUESSED, RelationType.COMPATIBLE, standardRelease, Objects.requireNonNull(compatibilityInfo, "compatibilityInfo"));
	}
}

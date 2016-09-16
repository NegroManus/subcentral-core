package de.subcentral.watcher.controller.processing;

import java.util.Objects;

import de.subcentral.core.metadata.release.Compatibility;
import de.subcentral.core.metadata.release.StandardRelease;

public class ProcessingResultInfo implements ProcessingInfo {
	public enum SourceType {
		LISTED, GUESSED
	}

	public enum RelationType {
		MATCH, COMPATIBLE, MANUAL
	}

	private ProcessingResult		result;
	private final SourceType		sourceType;
	private final RelationType		relationType;
	private final StandardRelease	standardRelease;
	private final Compatibility		compatibility;

	private ProcessingResultInfo(SourceType sourceType, RelationType relationType, StandardRelease standardRelease, Compatibility compatibility) {
		this.sourceType = Objects.requireNonNull(sourceType, "sourceType");
		this.relationType = Objects.requireNonNull(relationType, "relationType");
		this.standardRelease = standardRelease;
		this.compatibility = compatibility;
	}

	// package private
	void setResult(ProcessingResult result) {
		this.result = result;
	}

	public ProcessingResult getResult() {
		return result;
	}

	public SourceType getSourceType() {
		return sourceType;
	}

	public RelationType getRelationType() {
		return relationType;
	}

	public StandardRelease getStandardRelease() {
		return standardRelease;
	}

	public Compatibility getCompatibility() {
		return compatibility;
	}

	public static ProcessingResultInfo listedMatching() {
		return new ProcessingResultInfo(SourceType.LISTED, RelationType.MATCH, null, null);
	}

	public static ProcessingResultInfo listedCompatible(Compatibility compatibility) {
		return new ProcessingResultInfo(SourceType.LISTED, RelationType.COMPATIBLE, null, Objects.requireNonNull(compatibility, "compatibility"));
	}

	public static ProcessingResultInfo listedManual() {
		return new ProcessingResultInfo(SourceType.LISTED, RelationType.MANUAL, null, null);
	}

	public static ProcessingResultInfo guessedMatching(StandardRelease standardRelease) {
		return new ProcessingResultInfo(SourceType.GUESSED, RelationType.MATCH, standardRelease, null);
	}

	public static ProcessingResultInfo guessedCompatible(StandardRelease standardRelease, Compatibility compatibility) {
		return new ProcessingResultInfo(SourceType.GUESSED, RelationType.COMPATIBLE, standardRelease, Objects.requireNonNull(compatibility, "compatibility"));
	}
}

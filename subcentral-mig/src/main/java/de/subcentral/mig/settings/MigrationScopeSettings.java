package de.subcentral.mig.settings;

import java.util.List;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.media.Series;

public class MigrationScopeSettings {
	private boolean			includeAllSeries;
	private List<Series>	includedSeries	= ImmutableList.of();
	private boolean			includeSubtitles;

	public boolean getIncludeAllSeries() {
		return includeAllSeries;
	}

	public void setIncludeAllSeries(boolean includeAllSeries) {
		this.includeAllSeries = includeAllSeries;
	}

	public List<Series> getIncludedSeries() {
		return includedSeries;
	}

	public void setIncludedSeries(Iterable<Series> includedSeries) {
		this.includedSeries = ImmutableList.copyOf(includedSeries);
	}

	public boolean getIncludeSubtitles() {
		return includeSubtitles;
	}

	public void setIncludeSubtitles(boolean includeSubtitles) {
		this.includeSubtitles = includeSubtitles;
	}
}

package de.subcentral.core.media;

import java.time.temporal.Temporal;
import java.util.Set;

import de.subcentral.core.contribution.Work;
import de.subcentral.core.naming.Nameable;

public interface Media extends Nameable, Work
{
	public static final int	UNNUMBERED	= Integer.MAX_VALUE;

	// Property accessors
	public String getTitle();

	public Temporal getDate();

	public Set<String> getGenres();

	public String getDescription();

	public String getCoverUrl();
}
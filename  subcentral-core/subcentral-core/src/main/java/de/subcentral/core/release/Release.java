package de.subcentral.core.release;

import java.time.temporal.Temporal;
import java.util.List;

public interface Release<M> extends Comparable<Release<?>>
{
	public static final String	UNKNOWN_NUKE_REASON	= "";

	// Properties
	public String getName();

	public List<M> getMaterials();

	public Group getGroup();

	public List<Tag> getTags();

	public Temporal getDate();

	public String getSection();

	public long getSize();

	public String getNukeReason();

	public String getInfo();

	public String getInfoUrl();

	public String getSource();

	public String getSourceUrl();

	// Convenience
	public boolean containsSingleMaterial();

	public M getFirstMaterial();

	public boolean isNuked();
}

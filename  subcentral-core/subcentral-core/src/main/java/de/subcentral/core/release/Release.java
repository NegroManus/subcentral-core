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

	/**
	 * 
	 * @return The nuke reason. If <code>null</code>, the release is not nuked. Can be an empty String (<code>""</code>) if the release is nuked, but
	 *         the reason is unknown.
	 */
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

package de.subcentral.core.release;

import java.time.temporal.Temporal;
import java.util.List;

import de.subcentral.core.naming.Named;

public interface Release<M extends Named> extends Named, Comparable<Release<?>>
{
	// Properties
	public List<M> getMaterials();

	public Group getGroup();

	public List<Tag> getTags();

	public Temporal getDate();

	public String getNukeReason();

	public String getSection();

	public long getSize();

	public String getInfo();

	public String getInfoUrl();

	// Convenience
	public boolean containsSingleMaterial();

	public M getFirstMaterial();

	public boolean isNuked();
}

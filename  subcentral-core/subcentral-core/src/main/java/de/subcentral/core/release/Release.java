package de.subcentral.core.release;

import java.time.temporal.Temporal;
import java.util.List;

/**
 * A Release is a publication of one material (movie, TV episode, series, season, subtitle, song, album, movie, game, software) or a set of materials
 * (multiple TV episodes, multiple subtitles). Every Release has a unique name so that it can be identified. The name is constructed of:
 * <ul>
 * <li>the name(s) of the material(s)</li>
 * <li>the release tags</li>
 * <li>the release group</li>
 * </ul>
 * 
 * So, every Release contains a set of materials, has a list of release tags and is released by its release group.
 * 
 *
 * @param <M>
 *            The material class. For example {@link de.subcentral.core.media.Media} or {@link de.subcentral.core.subtitle.Subtitle}.
 */
public interface Release<M> extends Comparable<Release<?>>
{
	public static final String	UNKNOWN_NUKE_REASON	= "";

	// Properties
	/**
	 * 
	 * @return The unique name of this release (e.g. "Psych.S08E01.HDTV.x264-EXCELLENCE").
	 */
	public String getName();

	/**
	 * 
	 * @return The contained materials. For the most cases, a Release contains only one material. But, for example, multiple
	 *         {@link de.subcentral.core.media.Episode Episodes} are sometimes packed into one Release.
	 */
	public List<M> getMaterials();

	/**
	 * 
	 * @return The release group.
	 */
	public Group getGroup();

	/**
	 * @return The release tags (XviD, WEB-DL, DD5.1, 720p, ...).
	 */
	public List<Tag> getTags();

	/**
	 * 
	 * @return The release date.
	 */
	public Temporal getDate();

	/**
	 * 
	 * @return The release section.
	 */
	public String getSection();

	/**
	 * 
	 * @return The file size in bytes.
	 */
	public long getSize();

	/**
	 * 
	 * @return The nuke reason. If <code>null</code>, the release is not nuked. Can be an empty String (<code>""</code>) if the release is nuked, but
	 *         the reason is unknown.
	 */
	public String getNukeReason();

	/**
	 * 
	 * @return Information about this release (typically, the text of the nfo file).
	 */
	public String getInfo();

	/**
	 * 
	 * @return The URL of the nfo file.
	 */
	public String getInfoUrl();

	/**
	 * 
	 * @return The name of the source of the information about this release.
	 */
	public String getSource();

	/**
	 * 
	 * @return The URL of the source of the information about this release.
	 */
	public String getSourceUrl();

	// Convenience
	/**
	 * 
	 * @return Whether this release contains exactly one material (whether the size of the material list ({@link #getMaterials()}) is 1).
	 */
	public boolean containsSingleMaterial();

	/**
	 * 
	 * @return The first material in the material list or <code>null</code> if no materials.
	 */
	public M getFirstMaterial();

	/**
	 * 
	 * @return Whether this release is nuked (whether the nuke reason ({@link #getNukeReason()}) is not <code>null</code>).
	 */
	public boolean isNuked();
}

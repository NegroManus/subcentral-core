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

	/**
	 * <ul>
	 * <li>Audio: http://en.wikipedia.org/wiki/Parental_Advisory</li>
	 * <li>Video: http://en.wikipedia.org/wiki/Motion_picture_rating_system</li>
	 * <li>Games: http://en.wikipedia.org/wiki/Video_game_content_rating_system</li>
	 * </ul>
	 * 
	 * @return
	 */
	public String getContentRating();
}

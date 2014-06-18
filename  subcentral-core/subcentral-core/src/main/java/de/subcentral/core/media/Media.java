package de.subcentral.core.media;

import java.time.temporal.Temporal;
import java.util.Set;

import de.subcentral.core.contribution.Work;

public interface Media extends Work
{
	public static final int	UNNUMBERED	= Integer.MAX_VALUE;

	// Property accessors
	public String getName();

	public String getTitle();

	public Temporal getDate();

	public Set<String> getGenres();

	public String getOriginalLanguage();

	public Set<String> getCountriesOfOrigin();

	public String getDescription();

	public String getCoverUrl();

	/**
	 * <ul>
	 * <li>Audio: http://en.wikipedia.org/wiki/Parental_Advisory</li>
	 * <li>Video: http://en.wikipedia.org/wiki/Motion_picture_rating_system</li>
	 * <li>Games: http://en.wikipedia.org/wiki/Video_game_content_rating_system</li>
	 * </ul>
	 * 
	 * @return the content rating for this media
	 */
	public String getContentRating();
}

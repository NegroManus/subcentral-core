package de.subcentral.core.model.media;

public interface NamedMedia extends Media
{
	/**
	 * the unique name of this media
	 * 
	 * @return
	 */
	public String getName();

	/**
	 * For NamedMedia the title is the name without the extension to identify the media (e.g. without the year "(2014)" or country code "(UK)"). If
	 * the title equals the name, no title is specified.
	 * <p>
	 * Examples
	 * <ul>
	 * <li>name: "The Office (UK)" -> title: "The Office"</li>
	 * <li>name: "Titanic (2002)" -> title: "Titanic".</li>
	 * </ul>
	 * </p>
	 * 
	 * @return the {@link #getTitle() title} if this media {@link #isTitled() is titled}, else the {@link #getName() name}
	 */
	public String getTitleOrName();
}

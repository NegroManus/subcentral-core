package de.subcentral.core.naming;

import de.subcentral.core.media.Movie;
import de.subcentral.core.util.Replacer;
import de.subcentral.core.util.StringUtil;

public class MovieNamer implements Namer<Movie>
{
	private Replacer	nameReplacer	= null;
	private String		nameFormat		= "%s";

	public Replacer getNameReplacer()
	{
		return nameReplacer;
	}

	public void setNameReplacer(Replacer nameReplacer)
	{
		this.nameReplacer = nameReplacer;
	}

	public String getNameFormat()
	{
		return nameFormat;
	}

	public void setNameFormat(String nameFormat)
	{
		this.nameFormat = nameFormat;
	}

	@Override
	public Class<Movie> getType()
	{
		return Movie.class;
	}

	@Override
	public String name(Movie movie, NamingService namingService)
	{
		if (movie == null)
		{
			return null;
		}
		return String.format(nameFormat, StringUtil.replace(movie.getName(), nameReplacer));
	}
}

package de.subcentral.core.naming;

import java.util.Map;

import de.subcentral.core.util.Replacer;

public abstract class AbstractNamer<T> implements Namer<T>
{
	protected Replacer	wholeNameReplacer	= null;
	protected String	wholeNameFormat		= "%s";

	public Replacer getWholeNameReplacer()
	{
		return wholeNameReplacer;
	}

	public void setWholeNameReplacer(Replacer wholeNameReplacer)
	{
		this.wholeNameReplacer = wholeNameReplacer;
	}

	public String getWholeNameFormat()
	{
		return wholeNameFormat;
	}

	public void setWholeNameFormat(String wholeNameFormat)
	{
		this.wholeNameFormat = wholeNameFormat;
	}

	@Override
	public String name(T candidate, NamingService namingService, Map<String, Object> parameters) throws NamingException
	{
		if (candidate == null)
		{
			return null;
		}
		try
		{
			return formatWholeName(doName(candidate, namingService, parameters));
		}
		catch (Exception e)
		{
			throw new NamingException(candidate, e);
		}
	}

	/**
	 * 
	 * @param candidate
	 *            The candidate. Never null.
	 * @param namingService
	 *            The NamingService. May null.
	 * @param parameters
	 *            The parameters. Not null, may empty.
	 * @return The name of the candidate. Will be processed by {@link #formatWholeName(String)}.
	 * @throws Exception
	 *             Whatever exception occurs while naming the candidate. Will be wrapped into a NamingException and thrown.
	 */
	public abstract String doName(T candidate, NamingService namingService, Map<String, Object> parameters) throws Exception;

	// format methods
	public String formatWholeName(String wholeName)
	{
		return String.format(wholeNameFormat, Replacer.replace(wholeName, wholeNameReplacer));
	}
}

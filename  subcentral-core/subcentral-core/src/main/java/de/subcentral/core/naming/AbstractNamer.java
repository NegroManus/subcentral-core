package de.subcentral.core.naming;

public abstract class AbstractNamer<T> implements Namer<T>
{
	@Override
	public String name(T candidate, NamingService namingService) throws NamingException
	{
		if (candidate == null)
		{
			return null;
		}
		try
		{
			return doName(candidate, namingService);
		}
		catch (Exception e)
		{
			if (e instanceof NamingException)
			{
				throw (NamingException) e;
			}
			throw new NamingException(candidate, e);
		}
	}

	protected abstract String doName(T candidate, NamingService namingService) throws Exception;
}

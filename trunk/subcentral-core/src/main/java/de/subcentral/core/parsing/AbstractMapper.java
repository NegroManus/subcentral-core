package de.subcentral.core.parsing;

public abstract class AbstractMapper<T> implements Mapper<T>
{
	protected final PropParsingService	propParsingService;

	public AbstractMapper(PropParsingService propParsingService)
	{
		this.propParsingService = propParsingService;
	}

	public PropParsingService getPropParsingService()
	{
		return propParsingService;
	}
}

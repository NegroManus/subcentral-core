package de.subcentral.core.impl.com.orlydb;

public class OrlyDbQuery
{
	private final String	query;
	private final String	section;

	OrlyDbQuery(String query, String section)
	{
		// package-visibilty because can only be constructed by OrlyDbLookup
		this.section = section;
		this.query = query;
	}

	public String getQuery()
	{
		return query;
	}

	public String getSection()
	{
		return section;
	}
}

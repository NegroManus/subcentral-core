package de.subcentral.support.orlydbcom;

public class OrlyDbComLookupParameters
{
	private String	section;
	private String	query;

	public OrlyDbComLookupParameters()
	{

	}

	public OrlyDbComLookupParameters(String section, String query)
	{
		this.section = section;
		this.query = query;
	}

	public String getSection()
	{
		return section;
	}

	public void setSection(String section)
	{
		this.section = section;
	}

	public String getQuery()
	{
		return query;
	}

	public void setQuery(String query)
	{
		this.query = query;
	}
}

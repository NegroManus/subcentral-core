package de.subcentral.core.util;

public class PropString
{
	private final String	prop;
	private final Class<?>	type;

	public PropString(String prop)
	{
		this.prop = prop;
		this.type = null;
	}

	public PropString(String prop, Class<?> type)
	{
		this.prop = prop;
		this.type = type;
	}

	public String getProp()
	{
		return prop;
	}

	public Class<?> getType()
	{
		return type;
	}

}

package de.subcentral.core.util;

import com.google.common.base.MoreObjects;

public abstract class ServiceBase implements Service
{
	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(getClass()).add("name", getName()).toString();
	}
}
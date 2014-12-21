package de.subcentral.core.util;

import java.util.concurrent.ThreadFactory;

public class NamedThreadFactory implements ThreadFactory
{
	private final String	baseName;
	private int				num	= 0;

	public NamedThreadFactory(String baseName)
	{
		this.baseName = baseName;
	}

	@Override
	public Thread newThread(Runnable r)
	{
		if (num == Integer.MAX_VALUE)
		{
			num = 0;
		}
		return new Thread(r, new StringBuilder(baseName).append('#').append(num++).toString());
	}
}

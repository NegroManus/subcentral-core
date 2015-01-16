package de.subcentral.core.util;

import java.util.concurrent.ThreadFactory;
import java.util.function.IntFunction;

public class NumberedThreadFactory implements ThreadFactory
{
	private final IntFunction<Thread>	threadCreator;
	private int							num	= 0;

	public NumberedThreadFactory(IntFunction<Thread> threadCreator)
	{
		this.threadCreator = threadCreator;
	}

	@Override
	public Thread newThread(Runnable r)
	{
		if (num == Integer.MAX_VALUE)
		{
			num = 0;
		}
		return threadCreator.apply(num);
	}
}

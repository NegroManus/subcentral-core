/*
 * Copyright (C) 2008 Alexander Christian <alex(at)root1.de>. All rights reserved.
 * 
 * This file is part of SIMON.
 *
 *   SIMON is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SIMON is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with SIMON.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.subcentral.core.util;

import java.util.concurrent.ThreadFactory;

/**
 * A factory-class that let's you use named threads in a thread-pool
 * 
 * @author achristian
 * 
 */
public class NamedThreadPoolFactory implements ThreadFactory
{

	/** the base name for each thread created with this factory */
	private final String	baseName;

	private final boolean	addThreadNumber;

	private long			i	= 0;

	public NamedThreadPoolFactory(String baseName)
	{
		this(baseName, true);
	}

	public NamedThreadPoolFactory(String baseName, boolean addThreadNumber)
	{
		this.baseName = baseName;
		this.addThreadNumber = addThreadNumber;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
	 */
	public Thread newThread(Runnable r)
	{
		if ((i++) == Long.MAX_VALUE)
		{
			i = 0;
		}
		StringBuffer sb = new StringBuffer();
		sb.append(baseName);
		if (addThreadNumber)
		{
			sb.append(".#");
			sb.append(i);
		}
		return new Thread(r, sb.toString());
	}

}

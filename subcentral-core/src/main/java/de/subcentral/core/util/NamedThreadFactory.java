package de.subcentral.core.util;

import java.util.Objects;
import java.util.concurrent.ThreadFactory;

public class NamedThreadFactory implements ThreadFactory {
	private final String	baseName;
	private final boolean	daemon;
	private int				num	= 0;

	public NamedThreadFactory(String baseName) {
		this(baseName, false);
	}

	public NamedThreadFactory(String baseName, boolean daemon) {
		this.baseName = Objects.requireNonNull(baseName, "baseName");
		this.daemon = daemon;
	}

	@Override
	public Thread newThread(Runnable r) {
		if (num == Integer.MAX_VALUE) {
			num = 0;
		}
		Thread t = new Thread(r, new StringBuilder(baseName).append('-').append(num++).toString());
		t.setDaemon(daemon);
		return t;
	}
}

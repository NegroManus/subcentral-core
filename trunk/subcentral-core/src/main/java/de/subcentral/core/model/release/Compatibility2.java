package de.subcentral.core.model.release;

import java.util.function.Function;
import java.util.function.Predicate;

public class Compatibility2
{
	private final Predicate<Release>			matcher;
	private final Function<Release, Release>	compatibleReleaseBuilder;

	public Compatibility2(Predicate<Release> matcher, Function<Release, Release> compatibleReleaseBuilder)
	{
		this.matcher = matcher;
		this.compatibleReleaseBuilder = compatibleReleaseBuilder;
	}

}

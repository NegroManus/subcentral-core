package de.subcentral.core.metadata.release;

import java.util.Objects;

import com.google.common.base.MoreObjects;

public class Compatibility
{
	private final Release			source;
	private final Release			compatible;
	private final CompatibilityRule	rule;

	public Compatibility(Release source, Release compatible, CompatibilityRule rule)
	{
		this.source = Objects.requireNonNull(source);
		this.compatible = Objects.requireNonNull(compatible);
		this.rule = Objects.requireNonNull(rule);
	}

	public Release getSource()
	{
		return source;
	}

	public Release getCompatible()
	{
		return compatible;
	}

	public CompatibilityRule getRule()
	{
		return rule;
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(Compatibility.class).add("compatible", "source").add("source", source).add("rule", rule).toString();
	}
}
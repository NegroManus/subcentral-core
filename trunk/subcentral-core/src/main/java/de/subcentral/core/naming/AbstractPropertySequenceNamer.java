package de.subcentral.core.naming;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.UnaryOperator;

import de.subcentral.core.util.SeparationDefinition;

public abstract class AbstractPropertySequenceNamer<T> implements Namer<T>
{
	protected PropToStringService		propToStringService	= NamingStandards.getDefaultPropToStringService();
	protected Set<SeparationDefinition>	separators			= new HashSet<>(0);
	protected UnaryOperator<String>		wholeNameOperator;

	public PropToStringService getPropToStringService()
	{
		return propToStringService;
	}

	public void setPropToStringService(PropToStringService propToStringService)
	{
		this.propToStringService = Objects.requireNonNull(propToStringService, "propToStringService");
	}

	public Set<SeparationDefinition> getSeparators()
	{
		return separators;
	}

	public void setSeparators(Set<SeparationDefinition> separators)
	{
		this.separators = Objects.requireNonNull(separators, "separators");
	}

	public UnaryOperator<String> getWholeNameOperator()
	{
		return wholeNameOperator;
	}

	public void setWholeNameOperator(UnaryOperator<String> wholeNameOperator)
	{
		this.wholeNameOperator = wholeNameOperator;
	}

	@Override
	public String name(T candidate, Map<String, Object> parameters) throws NamingException
	{
		if (candidate == null)
		{
			return "";
		}
		try
		{
			PropSequenceNameBuilder builder = builder();
			buildName(builder, candidate, parameters);
			return builder.toString();
		}
		catch (Exception e)
		{
			throw new NamingException(candidate, e);
		}
	}

	public abstract void buildName(PropSequenceNameBuilder b, T candidate, Map<String, Object> parameters);

	private PropSequenceNameBuilder builder()
	{
		return new PropSequenceNameBuilder(propToStringService, separators, wholeNameOperator);
	}
}

package de.subcentral.core.naming;

public class NoNamerRegisteredException extends NamingException
{
	private static final long	serialVersionUID	= -3892446812297079146L;

	public NoNamerRegisteredException(Object candidate)
	{
		super(candidate, "No Namer registered for candidate's type");
	}
}

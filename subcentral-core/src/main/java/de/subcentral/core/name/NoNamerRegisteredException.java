package de.subcentral.core.name;

public class NoNamerRegisteredException extends NamingException
{
	private static final long serialVersionUID = -3892446812297079146L;

	public NoNamerRegisteredException(Object candidate, String message)
	{
		super(candidate, message);
	}
}

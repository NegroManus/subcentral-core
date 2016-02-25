package de.subcentral.watcher.controller.processing;

import java.util.Objects;

public class Status
{
	private final String	message;
	private final Throwable	exception;

	private Status(String message, Throwable exception)
	{
		this.message = Objects.requireNonNull(message, "message");
		this.exception = exception;
	}

	public String getMessage()
	{
		return message;
	}

	public Throwable getException()
	{
		return exception;
	}

	public static Status of(String message)
	{
		return new Status(message, null);
	}

	public static Status of(String message, Throwable exception)
	{
		return new Status(message, exception);
	}
}

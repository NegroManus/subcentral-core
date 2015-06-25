package de.subcentral.support.winrar;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public abstract class WinRarCommandConfig
{
    protected long     timeoutValue = 5;
    protected TimeUnit timeoutUnit  = TimeUnit.MINUTES;

    /**
     * The default value is {@code 15}.
     * 
     * @return the timeout value
     */
    public long getTimeoutValue()
    {
	return timeoutValue;
    }

    /**
     * The default value is {@link TimeUnit#SECONDS}.
     * 
     * @return the timeout unit
     */
    public TimeUnit getTimeoutUnit()
    {
	return timeoutUnit;
    }

    public void setTimeout(long timeout, TimeUnit unit)
    {
	this.timeoutValue = timeout;
	this.timeoutUnit = Objects.requireNonNull(unit, "unit");
    }
}

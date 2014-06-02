package de.subcentral.core.util;

public class TimeUtil
{

	public TimeUtil()
	{
		// TODO Auto-generated constructor stub
	}
	
	public static double durationMillis(long startNanos, long endNanos)
	{
		return (endNanos-startNanos) / 1_000_000d;
	}
	
	public static void main(String[] args)
	{
		System.out.println(durationMillis(1_000_000, 2_000_000));
	}

}

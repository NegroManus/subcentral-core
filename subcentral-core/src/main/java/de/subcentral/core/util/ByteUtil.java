package de.subcentral.core.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ByteUtil
{
	private static final Pattern BYTE_PATTERN = Pattern.compile("(\\d+(\\.\\d*)?)\\s*(K|M|G|T|P|E|Z|Y)?(i)?B(ytes)?", Pattern.CASE_INSENSITIVE);

	public static String bytesToString(long bytes, boolean si)
	{
		int unit = si ? 1000 : 1024;
		if (bytes < unit)
			return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPEZY" : "KMGTPEZY").charAt(exp - 1) + (si ? "" : "i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	public static long parseBytes(String s) throws NumberFormatException
	{
		Matcher mByte = BYTE_PATTERN.matcher(s);
		if (mByte.find())
		{
			double amount = Double.parseDouble(mByte.group(1));
			String unitPrefix = mByte.group(3);
			String i = mByte.group(4);

			if (unitPrefix == null)
			{
				return (long) amount;
			}
			double exponent = 0d;
			double unit = (i == null ? 1000d : 1024d);
			if (unitPrefix.equalsIgnoreCase("k"))
			{
				// kilobyte
				exponent = 1d;
			}
			else if (unitPrefix.equalsIgnoreCase("M"))
			{
				// megabyte
				exponent = 2d;
			}
			else if (unitPrefix.equalsIgnoreCase("G"))
			{
				// gigabyte
				exponent = 3d;
			}
			else if (unitPrefix.equalsIgnoreCase("T"))
			{
				// terabyte
				exponent = 4d;
			}
			else if (unitPrefix.equalsIgnoreCase("P"))
			{
				// petabyte
				exponent = 5d;
			}
			else if (unitPrefix.equalsIgnoreCase("E"))
			{
				// exabyte
				exponent = 6d;
			}
			else if (unitPrefix.equalsIgnoreCase("Z"))
			{
				// zettabyte
				exponent = 7d;
			}
			else if (unitPrefix.equalsIgnoreCase("Y"))
			{
				// yottabyte
				exponent = 8d;
			}
			return (long) (amount * Math.pow(unit, exponent));
		}
		throw new NumberFormatException("Bytes cannot be parsed from String: " + s);
	}

	private ByteUtil()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}

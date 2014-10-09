package de.subcentral.core.model;

import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class PatternTest
{

	public static void main(String[] args) throws ParseException
	{
		System.out.println(DecimalFormatSymbols.getInstance(Locale.ENGLISH).getDecimalSeparator());
		Format format = NumberFormat.getNumberInstance();
		Object o = format.parseObject("1.000,00");
		System.out.println(o);
	}
}

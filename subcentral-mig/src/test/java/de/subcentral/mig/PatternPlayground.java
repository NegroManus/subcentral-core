package de.subcentral.mig;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternPlayground
{

	public static void main(String[] args)
	{
		String text = "Übersetzung \"Ear2Sub\"";
		Pattern p = Pattern.compile("\\b(Übersetzung|Übersetzer|Übersetzt|Subbed by|Untertitel)\\b");

		Matcher m = p.matcher(text);
		System.out.println(text);
		while (m.find())
		{
			System.out.println(m.group());
		}
	}

}

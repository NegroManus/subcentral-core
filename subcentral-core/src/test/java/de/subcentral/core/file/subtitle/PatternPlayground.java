package de.subcentral.core.file.subtitle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternPlayground
{

	public static void main(String[] args)
	{
		String text = " smizz aka -TiLT-";
		Pattern p = Pattern.compile("(^|\\W)smizz aka -TiLT-(\\W|$)", Pattern.CASE_INSENSITIVE);

		Matcher m = p.matcher(text);
		while (m.find())
		{
			System.out.println(m.group());
		}
	}

}

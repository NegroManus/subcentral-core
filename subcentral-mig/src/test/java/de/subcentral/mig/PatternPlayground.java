package de.subcentral.mig;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternPlayground
{

    public static void main(String[] args)
    {
	String text = "VO: addic7ed.com";
	Pattern p = Pattern.compile("\\b(VO von|VO by|VO:|Transcript|Transkript|Subs|VO-Überarbeitung)\\b", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

	Matcher m = p.matcher(text);
	while (m.matches())
	{
	    System.out.println(m.group());
	}
    }

}

package de.subcentral.watcher.controller;

import java.util.Locale;

public class LocalePlayground
{

    public static void main(String[] args)
    {
	Locale china = Locale.SIMPLIFIED_CHINESE;
	System.out.println(china);
	System.out.println(china.toLanguageTag());
	System.out.println(china.getDisplayName());
	System.out.println(china.getDisplayScript());
	System.out.println(china.getDisplayVariant());

	Locale china2 = Locale.TRADITIONAL_CHINESE;
	System.out.println(china2);
	System.out.println(china2.toLanguageTag());
	System.out.println(china2.getDisplayName());
	System.out.println(china2.getDisplayScript());
	System.out.println(china2.getDisplayVariant());
    }

}

package de.subcentral.mig;

import org.jsoup.Jsoup;

public class NormalizePlayground {
    public static void main(String[] args) {
        String text = "<font face=\"Stencil\">www.addic7ed.com</font>";

        String normalizedText = Jsoup.parse(text).text();
        System.out.println(normalizedText);
    }
}

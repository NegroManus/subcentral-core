package tests;

import java.util.regex.Pattern;

import de.subcentral.core.util.ObjectUtil;

public class Test {
	public static void main(String[] args) {
		Pattern p1 = Pattern.compile("hello\\.\\d+", Pattern.CASE_INSENSITIVE);
		Pattern p2 = Pattern.compile("hello\\.\\d+", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

		System.out.println(ObjectUtil.patternsEqual(p1, p2));
	}
}

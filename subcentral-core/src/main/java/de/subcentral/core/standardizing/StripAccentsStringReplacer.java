package de.subcentral.core.standardizing;

import java.util.function.UnaryOperator;

import org.apache.commons.lang3.StringUtils;

public class StripAccentsStringReplacer implements UnaryOperator<String>
{
    @Override
    public String apply(String s)
    {
	return StringUtils.stripAccents(s);
    }
}

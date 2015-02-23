package de.subcentral.core.metadata.db;

import java.util.regex.Pattern;

import com.google.common.collect.ImmutableMap;

import de.subcentral.core.naming.DelegatingNamingService;
import de.subcentral.core.naming.NamingDefaults;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.util.CharReplacer;
import de.subcentral.core.util.PatternReplacer;

public class MetadataDbDefaults
{
	private static final NamingService	DEFAULT_METADATA_DB_NAMING_SERVICE	= initMetadataDbNamingService();

	public static NamingService getDefaultMetadataDbNamingService()
	{
		return DEFAULT_METADATA_DB_NAMING_SERVICE;
	}

	public static DelegatingNamingService createDefaultDelegatingMetadataDbNamingService(NamingService originalNamingService)
	{
		PatternReplacer pr = new PatternReplacer(ImmutableMap.of(Pattern.compile("&"), "and"));
		CharReplacer cr = new CharReplacer("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789", "'´`", ' ');
		return new DelegatingNamingService("QueryEntityNamingService", originalNamingService, pr.andThen(cr));
	}

	private static NamingService initMetadataDbNamingService()
	{
		return createDefaultDelegatingMetadataDbNamingService(NamingDefaults.getDefaultNamingService());
	}

	private MetadataDbDefaults()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}

package de.subcentral.mig;

import java.util.Locale;

public class Migration
{
	public static final Locale	LOCALE_GERMAN						= Locale.GERMAN;

	public static final String	SEASON_ATTR_THREAD_ID				= "boardId";
	public static final String	SEASON_IMG_TYPE_HEADER				= "header";

	public static final String	SERIES_ATTR_BOARD_ID				= "threadId";
	public static final String	SERIES_ATTR_SHORT_DESCRIPTION		= "shortDescription";
	public static final String	SERIES_IMG_TYPE_LOGO				= "logo";

	public static final String	SUBTITLE_LANGUAGE_GERMAN			= "de";
	public static final String	SUBTITLE_LANGUAGE_ENGLISH			= "en";

	public static final String	SUBTITLE_FILE_ATTR_ATTACHMENT_ID	= "attachmentId";

	public static final String	UNKNOWN_SERIES						= "UNKNOWN_SERIES";
	public static final String	UNKNOWN_SEASON						= "UNKNOWN_SEASON";
	public static final String	UNKNOWN_EPISODE						= "UNKNOWN_EPISODE";

	public static final int		TIMEOUT_MILLIS						= 10_000;
}

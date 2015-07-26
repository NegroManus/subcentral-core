package de.subcentral.core.metadata.release;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class TagUtilTest
{
	@Test
	public void testTransferMetaTags()
	{
		List<Tag> sourceTags = Tag.list("PROPER", "720p", "HDTV", "x264");
		List<Tag> targetTags = Tag.list("HDTV", "x264");
		List<Tag> metaTags = Tag.list("REAL", "PROPER", "REPACK", "DiRFIX", "NFOFIX", "iNTERNAL");
		TagUtil.transferMetaTags(sourceTags, targetTags, metaTags);
		assertEquals(Tag.list("PROPER", "HDTV", "x264"), targetTags);

		sourceTags = Tag.list("REAL", "PROPER", "720p", "HDTV", "x264");
		targetTags = Tag.list("HDTV", "x264");
		metaTags = Tag.list("REAL", "PROPER", "REPACK", "DiRFIX", "NFOFIX", "iNTERNAL");
		TagUtil.transferMetaTags(sourceTags, targetTags, metaTags);
		assertEquals(Tag.list("REAL", "PROPER", "HDTV", "x264"), targetTags);
	}
}

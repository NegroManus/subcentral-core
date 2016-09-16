package de.subcentral.core.metadata.release;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import de.subcentral.core.util.CollectionUtil;

public class TagUtilTest {
	@Test
	public void testTransferMetaTags() {
		List<Tag> sourceTags = Tags.of("PROPER", "720p", "HDTV", "x264");
		List<Tag> targetTags = Tags.of("HDTV", "x264");
		List<Tag> metaTags = Tags.of("REAL", "PROPER", "REPACK", "DiRFIX", "NFOFIX", "iNTERNAL");
		CollectionUtil.transferElementsToHead(sourceTags, targetTags, metaTags);
		assertEquals(Tags.of("PROPER", "HDTV", "x264"), targetTags);

		sourceTags = Tags.of("REAL", "PROPER", "720p", "HDTV", "x264");
		targetTags = Tags.of("HDTV", "x264");
		metaTags = Tags.of("REAL", "PROPER", "REPACK", "DiRFIX", "NFOFIX", "iNTERNAL");
		CollectionUtil.transferElementsToHead(sourceTags, targetTags, metaTags);
		assertEquals(Tags.of("REAL", "PROPER", "HDTV", "x264"), targetTags);
	}
}

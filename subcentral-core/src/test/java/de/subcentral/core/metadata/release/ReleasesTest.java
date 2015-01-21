package de.subcentral.core.metadata.release;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.ReleaseUtils;
import de.subcentral.core.metadata.release.Tag;

public class ReleasesTest
{
	@Test
	public void testStandardizeTags()
	{
		Release rls = new Release();
		rls.setTags(Tag.list("720", "WEB-DL", "DD5", "1", "x264"));
		ReleaseUtils.standardizeTags(rls);
		assertEquals(Tag.list("720", "WEB-DL", "DD5.1", "x264"), rls.getTags());
	}
}

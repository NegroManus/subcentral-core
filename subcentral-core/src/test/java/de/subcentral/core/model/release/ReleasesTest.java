package de.subcentral.core.model.release;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ReleasesTest
{

	@Test
	public void testStandardizeTags()
	{
		Release rls = new Release();
		rls.setTags(Tag.list("720", "WEB-DL", "DD5", "1", "x264"));
		Releases.standardizeTags(rls);
		assertEquals(Tag.list("720", "WEB-DL", "DD5.1", "x264"), rls.getTags());
	}

}

package de.subcentral.core.metadata.release;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import de.subcentral.core.standardizing.TypeStandardizingService;
import de.subcentral.core.standardizing.StandardizingChange;
import de.subcentral.core.standardizing.StandardizingDefaults;

public class ReleasesTest
{
	@Test
	public void testStandardizeTags()
	{
		Release rls = new Release();
		rls.setTags(Tag.list("720p", "WEB", "DL", "DD5", "1", "x264"));
		TypeStandardizingService service = new TypeStandardizingService("test");
		StandardizingDefaults.registerAllDefaultNestedBeansRetrievers(service);
		StandardizingDefaults.registerAllDefaulStandardizers(service);
		List<StandardizingChange> changes = service.standardize(rls);
		changes.forEach(c -> System.out.println(c));
		assertEquals(Tag.list("720p", "WEB-DL", "DD5.1", "x264"), rls.getTags());
	}
}

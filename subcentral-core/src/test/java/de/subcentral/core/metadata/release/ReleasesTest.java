package de.subcentral.core.metadata.release;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import de.subcentral.core.correction.Correction;
import de.subcentral.core.correction.CorrectionDefaults;
import de.subcentral.core.correction.TypeBasedCorrectionService;

public class ReleasesTest
{
	@Test
	public void testStandardizeTags()
	{
		Release rls = new Release();
		rls.setTags(Tag.list("720p", "WEB", "DL", "DD5", "1", "x264"));
		TypeBasedCorrectionService service = new TypeBasedCorrectionService("test");
		CorrectionDefaults.registerAllDefaultNestedBeansRetrievers(service);
		CorrectionDefaults.registerAllDefaultCorrectors(service);
		List<Correction> changes = service.correct(rls);
		changes.forEach(c -> System.out.println(c));
		assertEquals(Tag.list("720p", "WEB-DL", "DD5.1", "x264"), rls.getTags());
	}
}

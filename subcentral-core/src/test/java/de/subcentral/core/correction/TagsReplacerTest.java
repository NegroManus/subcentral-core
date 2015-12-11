package de.subcentral.core.correction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.util.List;

import org.junit.Test;

import de.subcentral.core.metadata.release.Tag;

public class TagsReplacerTest
{
	private final List<Tag>		input				= Tag.immutableList("WEB-DL", "DD5", "1", "H", "264");
	private final TagsReplacer	noOpReplacer		= new TagsReplacer(Tag.list("DD5", "2"), Tag.list("DD5.2"));
	private final TagsReplacer	dd51Replacer		= new TagsReplacer(Tag.list("DD5", "1"), Tag.list("DD5.1"));
	private final TagsReplacer	h264Replacer		= new TagsReplacer(Tag.list("H", "264"), Tag.list("H.264"));
	private final TagsReplacer	splitWebDlReplacer	= new TagsReplacer(Tag.list("WEB-DL"), Tag.list("WEB", "DL"));

	@Test
	public void testReplaceSequenceNoOp()
	{
		List<Tag> result = noOpReplacer.apply(input);
		assertSame(input, result);
	}

	@Test
	public void testReplaceSequenceDD51()
	{
		List<Tag> result = dd51Replacer.apply(input);
		assertEquals(Tag.list("WEB-DL", "DD5.1", "H", "264"), result);
		assertNotSame(input, result);
	}

	@Test
	public void testReplaceSequenceH264()
	{
		List<Tag> result = h264Replacer.apply(input);
		assertEquals(Tag.list("WEB-DL", "DD5", "1", "H.264"), result);
		assertNotSame(input, result);
	}

	@Test
	public void testReplaceSequenceDd51AndH264()
	{
		List<Tag> result = dd51Replacer.andThen(h264Replacer).apply(input);
		assertEquals(Tag.list("WEB-DL", "DD5.1", "H.264"), result);
		assertNotSame(input, result);
	}

	@Test
	public void testReplaceSequenceH264AndDd51()
	{
		List<Tag> result = h264Replacer.andThen(dd51Replacer).apply(input);
		assertEquals(Tag.list("WEB-DL", "DD5.1", "H.264"), result);
		assertNotSame(input, result);
	}

	@Test
	public void testReplaceSequenceSplitWebDl()
	{
		List<Tag> result = splitWebDlReplacer.apply(input);
		assertEquals(Tag.list("WEB", "DL", "DD5", "1", "H", "264"), result);
		assertNotSame(input, result);
	}
}

package de.subcentral.core.model.release;

import java.util.ListIterator;

public class Releases
{
	private Releases()
	{
		// utility class
	}

	public static Release standardizeTags(Release rls)
	{
		ListIterator<Tag> iter = rls.getTags().listIterator();
		Tag lastTag = null;
		while (iter.hasNext())
		{
			Tag tag = iter.next();
			if (lastTag != null)
			{
				// DD5.1
				if ("1".equals(tag.getName()) && "DD5".equals(lastTag.getName()))
				{
					lastTag.setName("DD5.1");
					iter.remove();
				}
				// H.264
				else if ("264".equals(tag.getName()) && "H".equals(lastTag.getName()))
				{
					lastTag.setName("H.264");
					iter.remove();
				}
			}
			lastTag = tag;
		}
		return rls;
	}
}

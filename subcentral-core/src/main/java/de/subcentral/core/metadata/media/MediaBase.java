package de.subcentral.core.metadata.media;

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

import de.subcentral.core.BeanUtil;

public abstract class MediaBase implements Media
{
	protected String								title;
	protected Temporal								date;
	protected String								description;
	protected Map<String, Float>					ratings				= new HashMap<>(2);
	protected String								contentRating;
	protected final ListMultimap<String, String>	images				= LinkedListMultimap.create(0);
	protected final List<String>					furtherInfoLinks	= new ArrayList<>(4);
	protected final ListMultimap<String, Object>	attributes			= LinkedListMultimap.create(0);

	@Override
	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	@Override
	public Temporal getDate()
	{
		return date;
	}

	public void setDate(Temporal date) throws IllegalArgumentException
	{
		this.date = BeanUtil.validateTemporalClass(date);
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	@Override
	public Map<String, Float> getRatings()
	{
		return ratings;
	}

	public void setRatings(Map<String, Float> ratings)
	{
		this.ratings.clear();
		this.ratings.putAll(ratings);
	}

	@Override
	public String getContentRating()
	{
		return contentRating;
	}

	public void setContentRating(String contentRating)
	{
		this.contentRating = contentRating;
	}

	@Override
	public ListMultimap<String, String> getImages()
	{
		return images;
	}

	public void setImages(ListMultimap<String, String> images)
	{
		this.images.clear();
		this.images.putAll(images);
	}

	@Override
	public List<String> getFurtherInfoLinks()
	{
		return furtherInfoLinks;
	}

	public void setFurtherInfoLinks(Collection<String> furtherInfoLinks)
	{
		this.furtherInfoLinks.clear();
		this.furtherInfoLinks.addAll(furtherInfoLinks);
	}

	@Override
	public ListMultimap<String, Object> getAttributes()
	{
		return attributes;
	}

	public void setAttributes(ListMultimap<String, Object> attributes)
	{
		this.attributes.clear();
		this.attributes.putAll(attributes);
	}
}

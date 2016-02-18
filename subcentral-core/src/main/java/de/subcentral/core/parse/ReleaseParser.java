package de.subcentral.core.parse;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.subcentral.core.metadata.media.Media;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.util.SimplePropDescriptor;

public class ReleaseParser extends AbstractMappingParser<Release>
{
	private final Mapper<? extends List<? extends Media>>	mediaMapper;
	private final Mapper<Release>							releaseMapper;

	public ReleaseParser(Iterable<MappingMatcher<SimplePropDescriptor>> matchers, Mapper<? extends List<? extends Media>> mediaMapper)
	{
		this(matchers, mediaMapper, ParsingDefaults.getDefaultReleaseMapper());
	}

	public ReleaseParser(Iterable<MappingMatcher<SimplePropDescriptor>> matchers, Mapper<? extends List<? extends Media>> mediaMapper, Mapper<Release> releaseMapper)
	{
		super(matchers);
		this.mediaMapper = Objects.requireNonNull(mediaMapper, "mediaMapper");
		this.releaseMapper = Objects.requireNonNull(releaseMapper, "releaseMapper");
	}

	public Mapper<? extends List<? extends Media>> getMediaMapper()
	{
		return mediaMapper;
	}

	public Mapper<Release> getReleaseMapper()
	{
		return releaseMapper;
	}

	@Override
	protected Release map(Map<SimplePropDescriptor, String> props)
	{
		// Media
		List<? extends Media> media = mediaMapper.map(props);

		// Release
		Release rls = releaseMapper.map(props);
		rls.setMedia(media);
		return rls;
	}
}

package de.subcentral.core.parsing;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.subcentral.core.model.media.Media;
import de.subcentral.core.model.release.Release;
import de.subcentral.core.util.SimplePropDescriptor;

public class ReleaseParser extends AbstractMappingParser<Release>
{
	private final Mapper<? extends List<? extends Media>>	mediaMapper;
	private final Mapper<Release>							releaseMapper	= Parsings.getDefaultReleaseMapper();

	public ReleaseParser(Mapper<? extends List<? extends Media>> mediaMapper)
	{
		this.mediaMapper = Objects.requireNonNull(mediaMapper, "mediaMapper");
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
		List<? extends Media> media = mediaMapper.map(props, propFromStringService);

		// Release
		Release rls = releaseMapper.map(props, propFromStringService);
		rls.getMedia().addAll(media);
		return rls;
	}
}

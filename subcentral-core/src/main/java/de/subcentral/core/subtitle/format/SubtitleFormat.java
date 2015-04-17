package de.subcentral.core.subtitle.format;

import java.nio.file.Path;

import de.subcentral.core.subtitle.SubtitleData;

public interface SubtitleFormat
{
	public SubtitleData read(Path file);

	public void write(SubtitleData sub, Path file);
}

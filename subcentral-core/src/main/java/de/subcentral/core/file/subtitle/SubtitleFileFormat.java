package de.subcentral.core.file.subtitle;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;

public interface SubtitleFileFormat
{
    public static final SubRip SUBRIP = new SubRip();

    public default SubtitleFile read(Path file, Charset charset) throws IOException
    {
	return read(Files.newBufferedReader(file, charset));
    }

    public default SubtitleFile read(InputStream inputStream, Charset charset) throws IOException
    {
	CharsetDecoder decoder = charset.newDecoder();
	Reader reader = new InputStreamReader(inputStream, decoder);
	return read(new BufferedReader(reader));
    }

    public SubtitleFile read(BufferedReader reader) throws IOException;

    public default void write(SubtitleFile sub, Path file, Charset charset, OpenOption... options) throws IOException
    {
	write(sub, Files.newBufferedWriter(file, charset, options));
    }

    public default void write(SubtitleFile sub, OutputStream outputStream, Charset charset) throws IOException
    {
	CharsetEncoder encoder = charset.newEncoder();
	Writer writer = new OutputStreamWriter(outputStream, encoder);
	write(sub, new BufferedWriter(writer));
    }

    public void write(SubtitleFile sub, BufferedWriter writer) throws IOException;
}

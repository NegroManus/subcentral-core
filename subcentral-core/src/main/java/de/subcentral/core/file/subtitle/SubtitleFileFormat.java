package de.subcentral.core.file.subtitle;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
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

public interface SubtitleFileFormat {
    public String getName();

    public String getExtension();

    public String getContentType();

    public default SubtitleContent read(Path file, Charset charset) throws IOException {
        return read(Files.newBufferedReader(file, charset));
    }

    public default SubtitleContent read(InputStream inputStream, Charset charset) throws IOException {
        CharsetDecoder decoder = charset.newDecoder();
        Reader reader = new InputStreamReader(inputStream, decoder);
        return read(new BufferedReader(reader));
    }

    public default SubtitleContent read(byte[] bytes, Charset charset) throws IOException {
        CharsetDecoder decoder = charset.newDecoder();
        Reader reader = new InputStreamReader(new ByteArrayInputStream(bytes), decoder);
        return read(new BufferedReader(reader));
    }

    public SubtitleContent read(BufferedReader reader) throws IOException;

    public default void write(SubtitleContent sub, Path file, Charset charset, OpenOption... options) throws IOException {
        write(sub, Files.newBufferedWriter(file, charset, options));
    }

    public default void write(SubtitleContent sub, OutputStream outputStream, Charset charset) throws IOException {
        CharsetEncoder encoder = charset.newEncoder();
        Writer writer = new OutputStreamWriter(outputStream, encoder);
        write(sub, new BufferedWriter(writer));
    }

    public void write(SubtitleContent sub, BufferedWriter writer) throws IOException;
}

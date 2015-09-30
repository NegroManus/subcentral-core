package de.subcentral.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StreamGobbler implements Runnable
{
	private static final Logger log = LogManager.getLogger(StreamGobbler.class);

	private final InputStream	input;
	private final OutputStream	output;

	public StreamGobbler(InputStream input, OutputStream output)
	{
		this.input = input;
		this.output = output;
	}

	@Override
	public void run()
	{
		try
		{
			byte[] buffer = new byte[1024]; // Adjust if you want
			int bytesRead;
			while ((bytesRead = input.read(buffer)) != -1)
			{
				System.out.println(new String(buffer, StandardCharsets.ISO_8859_1));
				output.write(buffer, 0, bytesRead);
			}
			// log.debug("Completely written input {} to output {}", input, output);
		}
		catch (IOException e)
		{
			log.error("Exception while writing from input " + input + " to output " + output, e);
		}
	}
}

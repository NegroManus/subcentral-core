package de.subcentral.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamGobbler extends Thread
{
	private final InputStream	input;
	private OutputStream		output;

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
				output.write(buffer, 0, bytesRead);
			}
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
	}
}

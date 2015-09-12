package de.subcentral.core.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.MoreObjects;

public class IOUtil
{
	private static final Logger log = LogManager.getLogger(IOUtil.class.getName());

	public static final class ProcessResult
	{
		private final int		exitValue;
		private final String	stdOut;
		private final String	stdErr;

		private ProcessResult(int exitValue, String logMessage, String errorMessage)
		{
			this.exitValue = exitValue;
			this.stdOut = logMessage;
			this.stdErr = errorMessage;
		}

		public int getExitValue()
		{
			return exitValue;
		}

		/**
		 * 
		 * @return the standard output or <code>null</code> if none
		 */
		public String getStdOut()
		{
			return stdOut;
		}

		/**
		 * 
		 * @return the standard error output or <code>null</code> if none
		 */
		public String getStdErr()
		{
			return stdErr;
		}

		@Override
		public String toString()
		{
			return MoreObjects.toStringHelper(ProcessResult.class).omitNullValues().add("exitValue", exitValue).add("stdOut", stdOut).add("stdErr", stdErr).toString();
		}
	}

	public static ProcessResult executeProcess(List<String> command, long timeoutValue, TimeUnit timeoutUnit) throws IOException, InterruptedException, TimeoutException
	{
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		log.debug("Executing {} with directory={}; environment={}; timeout={}", command, processBuilder.directory(), processBuilder.environment(), timeoutValue, timeoutUnit);
		Process process = processBuilder.start();
		process.getOutputStream().close();

		// StdOut and StdErr can be written in parallel so they need be read in dedicated Threads
		ByteArrayOutputStream stdOutStream = new ByteArrayOutputStream();
		StreamGobbler stdOutGobbler = new StreamGobbler(process.getInputStream(), stdOutStream);
		ByteArrayOutputStream stdErrStream = new ByteArrayOutputStream();
		StreamGobbler stdErrGobbler = new StreamGobbler(process.getErrorStream(), stdErrStream);
		stdOutGobbler.start();
		stdErrGobbler.start();

		boolean exitedBeforeTimeout = process.waitFor(timeoutValue, timeoutUnit);
		if (!exitedBeforeTimeout)
		{
			throw new TimeoutException("Process execution did not finish before timeout was reached. command=" + command + ", timeout=" + timeoutValue + " " + timeoutUnit);
		}
		String stdOut = StringUtils.stripToNull(stdOutStream.toString(Charset.defaultCharset().name()));
		String stdErr = StringUtils.stripToNull(stdErrStream.toString(Charset.defaultCharset().name()));
		ProcessResult result = new ProcessResult(process.exitValue(), stdOut, stdErr);
		log.debug("Process execution result: {}", result);
		return result;
	}

	/**
	 * Drains the InputStream to a String<b>and</b> closes it.
	 * 
	 * @param is
	 * @return
	 */
	public static String drainToString(InputStream is)
	{
		if (is == null)
		{
			return "";
		}
		try (Scanner s = new Scanner(is, Charset.defaultCharset().name()))
		{
			return s.useDelimiter("\\A").hasNext() ? s.next() : "";
		}
	}

	public static String[] splitIntoFilenameAndExtension(String filename)
	{
		if (filename == null)
		{
			return new String[]
			{ "", "" };
		}
		int indexOfLastDot = filename.lastIndexOf('.');
		if (indexOfLastDot == -1)
		{
			return new String[]
			{ filename, "" };
		}
		return new String[]
		{ filename.substring(0, indexOfLastDot), filename.substring(indexOfLastDot, filename.length()) };
	}

	public static boolean isCompletelyWritten(Path file)
	{
		RandomAccessFile stream = null;
		try
		{
			stream = new RandomAccessFile(file.toFile(), "rw");
			return true;
		}
		catch (Exception e)
		{
			// ignore
			// e.printStackTrace();
		}
		finally
		{
			if (stream != null)
			{
				try
				{
					stream.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	public static boolean waitUntilCompletelyWritten(Path file, long timeout, TimeUnit timeoutUnit) throws TimeoutException
	{
		long start = System.currentTimeMillis();
		while (!isCompletelyWritten(file))
		{
			if (System.currentTimeMillis() >= start + timeoutUnit.toMillis(timeout))
			{
				throw new TimeoutException("Timed out after " + timeout + " " + timeoutUnit + " while waiting for file " + file + " to become completely written (unlocked)");
			}
			try
			{
				Thread.sleep(50);
			}
			catch (InterruptedException e)
			{
				// ignore
				e.printStackTrace();
			}
		}
		return true;
	}

	public static void unzip(Path archive, Path targetDir, boolean flat) throws IOException
	{
		byte[] buffer = new byte[1024];

		ZipInputStream zis = null;
		try
		{
			// get the zip file content
			zis = new ZipInputStream(Files.newInputStream(archive));
			// get the zipped file list entry
			ZipEntry ze;
			while ((ze = zis.getNextEntry()) != null)
			{
				Path filePath = Paths.get(ze.getName());
				Path filename = filePath.getFileName();
				Path extractedFile = targetDir.resolve(flat ? filename : filePath);
				System.out.println("File path: " + filePath);
				System.out.println("File name: " + filename);
				System.out.println("file unzip : " + extractedFile);

				if (ze.isDirectory())
				{
					if (!flat)
					{
						// create all non exists folders
						// else you will hit FileNotFoundException for compressed folder
						Files.createDirectories(extractedFile);
					}
				}
				else
				{
					try (OutputStream fos = Files.newOutputStream(extractedFile);)
					{
						int len;
						while ((len = zis.read(buffer)) > 0)
						{
							fos.write(buffer, 0, len);
						}
					}
				}
			}
		}
		finally
		{
			try
			{
				if (zis != null)
				{
					zis.closeEntry();
					zis.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * UTF-8 is not supposed to have a Byte Order Mark (BOM). But it can happen. This method removes leading BOMs.
	 * 
	 * @param s
	 *            string
	 * @return string without leading UTF-8 BOM
	 */
	public static String removeUTF8BOM(String s)
	{
		if (s == null)
		{
			return null;
		}
		// FEFF because this is the Unicode char represented by the UTF-8 byte order mark (EF BB BF).
		final String UTF8_BOM = "\uFEFF";
		if (s.startsWith(UTF8_BOM))
		{
			s = s.substring(1);
		}
		return s;
	}

	public static boolean pingHttp(URL url, int timeout)
	{
		return pingHttp(url.toExternalForm(), timeout);
	}

	/**
	 * Pings a HTTP URL. This effectively sends a HEAD request and returns <code>true</code> if the response code is in the 200-399 range.
	 * 
	 * @param url
	 *            The HTTP URL to be pinged.
	 * @param timeout
	 *            The timeout in millis for both the connection timeout and the response read timeout. Note that the total timeout is effectively two times the given timeout.
	 * @return <code>true</code> if the given HTTP URL has returned response code 200-399 on a HEAD request within the given timeout, otherwise <code>false</code>.
	 */
	public static boolean pingHttp(String url, int timeout)
	{
		// Otherwise an exception may be thrown on invalid SSL certificates:
		url = url.replaceFirst("^https", "http");

		try
		{
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setConnectTimeout(timeout);
			connection.setReadTimeout(timeout);
			connection.setRequestMethod("HEAD");
			int responseCode = connection.getResponseCode();
			return (200 <= responseCode && responseCode <= 399);
		}
		catch (IOException exception)
		{
			return false;
		}
	}

	public static String getDomainName(URL url) throws URISyntaxException
	{
		return getDomainName(url.toURI());
	}

	public static String getDomainName(String url) throws URISyntaxException
	{
		return getDomainName(new URI(url));
	}

	public static String getDomainName(URI uri)
	{
		String domain = uri.getHost();
		return domain.startsWith("www.") ? domain.substring(4) : domain;
	}

	private IOUtil()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}

package de.subcentral.core.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
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
	private static final Logger log = LogManager.getLogger(IOUtil.class);

	private IOUtil()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}

	public static ProcessResult executeProcess(List<String> command, long timeoutValue, TimeUnit timeoutUnit) throws IOException, InterruptedException, TimeoutException
	{
		return executeProcess(command, timeoutValue, timeoutUnit, null);
	}

	/**
	 * 
	 * @param command
	 * @param timeoutValue
	 * @param timeoutUnit
	 * @param executor
	 *            if you want to use threads of an ExecutorService to gobble the stdOut and stdErr streams. <b>IMPORTANT:</b> The executor has to have 2 threads available otherwise the sub process
	 *            won't exit because its streams are not closed.
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	public static ProcessResult executeProcess(List<String> command, long timeoutValue, TimeUnit timeoutUnit, ExecutorService executor) throws IOException, InterruptedException, TimeoutException
	{
		Process process = null;
		try
		{
			ProcessBuilder processBuilder = new ProcessBuilder(command);
			log.debug("Executing process {} with directory={}; environment={}; timeout={} {}", command, processBuilder.directory(), processBuilder.environment(), timeoutValue, timeoutUnit);
			long start = System.nanoTime();
			process = processBuilder.start();
			process.getOutputStream().close();

			// StdOut and StdErr can be written in parallel so they need be read in dedicated Threads
			ByteArrayOutputStream stdOutStream = new ByteArrayOutputStream();
			StreamGobbler stdOutGobbler = new StreamGobbler(process.getInputStream(), stdOutStream);
			ByteArrayOutputStream stdErrStream = new ByteArrayOutputStream();
			StreamGobbler stdErrGobbler = new StreamGobbler(process.getErrorStream(), stdErrStream);
			if (executor != null)
			{
				executor.submit(stdOutGobbler);
				executor.submit(stdErrGobbler);
			}
			else
			{
				new Thread(stdOutGobbler).start();
				new Thread(stdErrGobbler).start();
			}

			boolean exitedBeforeTimeout = process.waitFor(timeoutValue, timeoutUnit);
			if (!exitedBeforeTimeout)
			{
				throw new TimeoutException("Process execution did not finish before timeout was reached. command=" + command + ", timeout=" + timeoutValue + " " + timeoutUnit);
			}
			String stdOut = StringUtils.stripToNull(stdOutStream.toString(Charset.defaultCharset().name()));
			String stdErr = StringUtils.stripToNull(stdErrStream.toString(Charset.defaultCharset().name()));
			ProcessResult result = new ProcessResult(process.exitValue(), stdOut, stdErr);
			log.debug("Executed process {} in {} ms with result: {}", command, TimeUtil.durationMillis(start), result);
			return result;
		}
		catch (Exception e)
		{
			// Clean up
			if (process != null)
			{
				process.destroy();
			}
			throw e;
		}
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
			return new String[] { "", "" };
		}
		int indexOfLastDot = filename.lastIndexOf('.');
		if (indexOfLastDot == -1)
		{
			return new String[] { filename, "" };
		}
		return new String[] { filename.substring(0, indexOfLastDot), filename.substring(indexOfLastDot, filename.length()) };
	}

	public static boolean isCompletelyWritten(Path file)
	{
		try (RandomAccessFile raf = new RandomAccessFile(file.toFile(), "rw"))
		{
			return true;
		}
		catch (Exception e)
		{
			log.trace("File is not completely written yet", e);
			return false;
		}
	}

	public static boolean isLocked(Path file) throws IOException
	{
		try (SeekableByteChannel channel = Files.newByteChannel(file, StandardOpenOption.WRITE))
		{
			return false;
		}
		catch (FileSystemException e)
		{
			log.trace("File is locked", e);
			return true;
		}
	}

	public static boolean waitUntilUnlocked(Path file, long timeout, TimeUnit timeoutUnit) throws IOException, TimeoutException, InterruptedException
	{
		long start = System.currentTimeMillis();
		boolean waited = false;
		while (isLocked(file))
		{
			if (waited == false)
			{
				waited = true;
			}
			if (System.currentTimeMillis() >= start + timeoutUnit.toMillis(timeout))
			{
				throw new TimeoutException("Timed out after " + timeout + " " + timeoutUnit + " while waiting for file " + file + " to become unlocked");
			}
			log.debug("Waiting on file {} to be written", file);
			Thread.sleep(100);
		}
		return waited;
	}

	public static boolean waitUntilSizeRemainsUnchanged(Path file, long checkPeriod, long timeout, TimeUnit timeoutUnit) throws IOException, TimeoutException, InterruptedException
	{
		long start = System.currentTimeMillis();
		boolean waited = false;
		for (;;)
		{
			if (System.currentTimeMillis() >= start + timeoutUnit.toMillis(timeout))
			{
				throw new TimeoutException("Timed out after " + timeout + " " + timeoutUnit + " while waiting for file size of " + file + " to remain unchanged for " + checkPeriod + " ms");
			}

			long oldSize = Files.size(file);
			Thread.sleep(checkPeriod);
			long newSize = Files.size(file);
			if (oldSize == newSize)
			{
				break;
			}

			if (!waited)
			{
				waited = true;
			}
			log.debug("Waiting until file size of {} remains unchanged for {} ms", file, checkPeriod);
		}
		return waited;
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
				log.warn("Exception while closing ZipInputStream", e);
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
		final String utf8BOM = "\uFEFF";
		if (s.startsWith(utf8BOM))
		{
			return s.substring(1);
		}
		return s;
	}

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
}

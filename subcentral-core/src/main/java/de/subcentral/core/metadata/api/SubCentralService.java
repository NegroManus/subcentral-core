package de.subcentral.core.metadata.api;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Joiner;

public class SubCentralService
{
	private static final Logger		log		= LogManager.getLogger(SubCentralService.class);
	private static final Charset	UTF_8	= Charset.forName("UTF-8");

	private String					cookies;

	public void login(String username, String password) throws IOException
	{
		String body = "loginUsername=" + URLEncoder.encode(username, UTF_8.name()) + "&loginPassword=" + URLEncoder.encode(password, UTF_8.name());

		URL loginUrl = new URL("http://subcentral.de/index.php?form=UserLogin");
		HttpURLConnection conn = (HttpURLConnection) loginUrl.openConnection();
		conn.setRequestMethod("POST");
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("Content-Length", String.valueOf(body.length()));

		try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), UTF_8);)
		{
			writer.write(body);
		}

		List<String> cookieList = new ArrayList<>(4);
		Map<String, List<String>> map = conn.getHeaderFields();
		for (Map.Entry<String, List<String>> entry : map.entrySet())
		{
			if ("Set-Cookie".equalsIgnoreCase(entry.getKey()))
			{
				cookieList.addAll(entry.getValue());
			}
		}
		cookies = Joiner.on("; ").join(cookieList);
		log.debug("Logged in as {} (Cookie: {})", username, cookies);
	}

	public void logout() throws IOException
	{
		HttpURLConnection conn = openConnectionWithCookies(new URL("http://subcentral.de/index.php?action=UserLogout"));
		conn.connect();
		cookies = null;
		log.debug("Logged out");
	}

	public void downloadAttachment(int attachmentId, Path folder) throws IOException
	{
		HttpURLConnection conn = openConnectionWithCookies(new URL("http://subcentral.de/index.php?page=Attachment&attachmentID=" + attachmentId));

		String filename = null;
		String contentDisposition = conn.getHeaderField("Content-disposition");
		if (contentDisposition != null)
		{
			Pattern filenamePattern = Pattern.compile("filename=\"(.*)\"");
			Matcher filenameMatcher = filenamePattern.matcher(contentDisposition);
			if (filenameMatcher.find())
			{
				filename = filenameMatcher.group(1);
			}
		}
		long contentLength = conn.getHeaderFieldLong("Content-Length", 0L);
		String contentType = conn.getHeaderField("Content-Type");

		log.debug("Downloading attachment, id={}, filename={}, contentType={}, contentLength={}", attachmentId, filename, contentType, contentLength);

		/**
		 * Header
		 * 
		 * <pre>
		 * HTTP/1.1 200 OK
		 * Date: Fri, 29 May 2015 11:27:42 GMT
		 * Server: Apache
		 * Content-disposition: attachment; filename="Psych.S02E15.HDTV.XviD-LOL.ger.zip"
		 * Pragma: no-cache
		 * Expires: 0
		 * Content-Length: 25301
		 * Content-Type: application/x-zip-compressed
		 * Proxy-Connection: Keep-Alive
		 * Connection: Keep-Alive
		 * </pre>
		 */
		ReadableByteChannel rbc = Channels.newChannel(conn.getInputStream());
		try (FileOutputStream fos = new FileOutputStream(folder.resolve(filename).toString());)
		{
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		}
		log.debug("Downloaded attachment, id={}, filename={}, contentType={}, contentLength={}", attachmentId, filename, contentType, contentLength);
	}

	private HttpURLConnection openConnectionWithCookies(URL url) throws IOException
	{
		URLConnection conn = url.openConnection();
		if (cookies != null)
		{
			conn.setRequestProperty("Cookie", cookies);
		}
		return (HttpURLConnection) conn;
	}

	private void printHeaders(URLConnection conn)
	{
		Map<String, List<String>> map = conn.getHeaderFields();
		for (Map.Entry<String, List<String>> entry : map.entrySet())
		{
			System.out.println("Key=" + entry.getKey() + ", Value=" + entry.getValue());
		}
	}

}

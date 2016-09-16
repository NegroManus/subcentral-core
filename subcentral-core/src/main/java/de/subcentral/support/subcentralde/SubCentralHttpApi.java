package de.subcentral.support.subcentralde;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.common.base.Joiner;

public class SubCentralHttpApi implements SubCentralApi {
	private static final Logger		log		= LogManager.getLogger(SubCentralHttpApi.class);
	private static final Charset	UTF_8	= Charset.forName("UTF-8");
	private static final URL		host	= initHost();

	private static URL initHost() {
		try {
			return new URL("https://www.subcentral.de/");
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static URL getHost() {
		return host;
	}

	private String cookies;

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.subcentral.support.subcentralde.SubCentralApi#login(java.lang.String, java.lang.String)
	 */
	@Override
	public void login(String username, String password, boolean stayLoggedIn) throws IOException {
		log.debug("Loggin in as {}", username);
		URL loginUrl = new URL(host, "index.php?form=UserLogin");
		StringBuilder body = new StringBuilder();
		body.append("loginUsername=");
		body.append(URLEncoder.encode(username, UTF_8.name()));
		body.append("&loginPassword=");
		body.append(URLEncoder.encode(password, UTF_8.name()));
		if (stayLoggedIn) {
			body.append("&useCookies=1");
		}

		HttpURLConnection conn = openConnection(loginUrl);
		conn.setRequestMethod("POST");
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("Content-Length", String.valueOf(body.length()));

		try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), UTF_8);) {
			writer.write(body.toString());
		}

		if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
			throw new IllegalStateException("Login failed. Server did not return response code 200: " + conn.getHeaderField(null));
		}
		InputStream is = conn.getInputStream();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
			boolean success = false;
			/**
			 * <pre>
			 * <div class="success">
			<p>Sie wurden erfolgreich angemeldet.</p>
			<p><a href="index.php?s=fbc3ebe533737773a6a95a0f016bbf488fad601c">Falls die automatische Weiterleitung nicht funktioniert, klicken Sie bitte hier!</a></p>
			</div>
			 * </pre>
			 */
			Matcher successMatcher = Pattern.compile("<div class=\"success\">").matcher("");
			Matcher errorMatcher = Pattern.compile("<p class=\"error\">(.*?)</p>").matcher("");
			String line;
			while ((line = reader.readLine()) != null) {
				if (successMatcher.reset(line).find()) {
					success = true;
					break;

				}
				else if (errorMatcher.reset(line).find()) {
					throw new IllegalArgumentException("Login failed: " + errorMatcher.group(1));
				}
			}
			if (success == false) {
				throw new IllegalArgumentException("Login failed. No success message");
			}
		}

		List<String> cookieList = new ArrayList<>(4);
		Map<String, List<String>> map = conn.getHeaderFields();
		for (Map.Entry<String, List<String>> entry : map.entrySet()) {
			if ("Set-Cookie".equalsIgnoreCase(entry.getKey())) {
				cookieList.addAll(entry.getValue());
			}
		}

		cookies = Joiner.on("; ").join(cookieList).replace("; HttpOnly", "");
		if (cookies.isEmpty()) {
			throw new IllegalStateException("Login failed. Server failed to send session cookie.");
		}
		log.debug("Logged in as {} (Cookies: {})", username, cookies);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.subcentral.support.subcentralde.SubCentralApi#logout()
	 */
	@Override
	public void logout() throws IOException {
		// TODO
		// <a href="index.php?action=UserLogout&t=3b544a6570f7faad547354dfe1f7900fb2fb3753"><img src="wcf/icon/logoutS.png" alt=""> <span>Abmelden</span></a>
		HttpsURLConnection conn = openConnection(new URL(host, "index.php?action=UserLogout"));
		conn.connect();
		if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
			log.warn("Logout failed. Server did not return response code 200: " + conn.getHeaderField(null));
		}
		String oldCookies = cookies;
		cookies = null;
		log.debug("Logged out (Cookies: {})", oldCookies);
	}

	@Override
	public Document getContent(String url) throws IOException {
		log.debug("Getting content of {}", url);
		HttpsURLConnection conn = openConnection(new URL(host, url));
		Document doc = Jsoup.parse(conn.getInputStream(), StandardCharsets.UTF_8.name(), host.toExternalForm());
		log.debug("Got content of {}", url);
		return doc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.subcentral.support.subcentralde.SubCentralApi#downloadAttachment(int, java.nio.file.Path)
	 */
	@Override
	public Path downloadAttachment(int attachmentId, Path directory) throws IOException {
		HttpsURLConnection conn = openConnection(new URL(host + "index.php?page=Attachment&attachmentID=" + attachmentId));

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
		String filename = null;
		String contentDisposition = conn.getHeaderField("Content-disposition");
		if (contentDisposition != null) {
			Pattern filenamePattern = Pattern.compile("filename=\"(.*?)\"");
			Matcher filenameMatcher = filenamePattern.matcher(contentDisposition);
			if (filenameMatcher.find()) {
				filename = filenameMatcher.group(1);
			}
		}
		long contentLength = conn.getHeaderFieldLong("Content-Length", 0L);
		String contentType = conn.getHeaderField("Content-Type");

		log.debug("Downloading attachment. id={}, filename={}, contentType={}, contentLength={}", attachmentId, filename, contentType, contentLength);
		long start = System.currentTimeMillis();
		ReadableByteChannel rbc = Channels.newChannel(conn.getInputStream());
		Path target = directory.resolve(filename);
		try (FileOutputStream fos = new FileOutputStream(target.toFile());) {
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		}
		long duration = System.currentTimeMillis() - start;
		log.debug("Downloaded attachment in {} ms. id={}, filename={}, contentType={}, contentLength={}", duration, attachmentId, filename, contentType, contentLength);
		return target;
	}

	private HttpsURLConnection openConnection(URL url) throws IOException {
		URLConnection conn = url.openConnection();
		if (cookies != null) {
			conn.setRequestProperty("Cookie", cookies);
		}
		// fake mozilla user agent
		conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:31.0) Gecko/20100101 Firefox/31.0");
		return (HttpsURLConnection) conn;
	}

	private void printHeaders(URLConnection conn) {
		Map<String, List<String>> map = conn.getHeaderFields();
		for (Map.Entry<String, List<String>> entry : map.entrySet()) {
			System.out.println("Key=" + entry.getKey() + ", Value=" + entry.getValue());
		}
	}
}

package de.subcentral.core.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Function;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;

public class NetUtil {
    private static final Logger log = LogManager.getLogger(NetUtil.class);

    private NetUtil() {
        throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
    }

    public static String formatQueryString(Map<String, String> keyValuePairs) {
        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> pair : keyValuePairs.entrySet()) {
            if (query.length() > 0) {
                query.append("&");
            }
            appendToQueryBuilder(query, pair.getKey(), pair.getValue());
        }
        return query.toString();
    }

    public static String formatQueryString(String key, String value) {
        return appendToQueryBuilder(new StringBuilder(), key, value).toString();
    }

    private static StringBuilder appendToQueryBuilder(StringBuilder builder, String key, String value) {
        try {
            builder.append(key);
            builder.append('=');
            // URLEncoder is just for encoding queries, not for the whole URL
            if (value != null) {
                builder.append(URLEncoder.encode(value, StandardCharsets.UTF_8.name()));
            }
            return builder;
        }
        catch (UnsupportedEncodingException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public static String getDomainName(String url) throws URISyntaxException {
        return getDomainName(new URI(url));
    }

    public static String getDomainName(URL url) throws URISyntaxException {
        return getDomainName(url.toURI());
    }

    public static String getDomainName(URI uri) {
        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }

    /**
     * Pings a HTTP URL. This effectively sends a GET request and returns the response time if the response code is in the 200-399 range.
     * 
     * @param url
     *            The HTTP URL to be pinged.
     * @param timeout
     *            The timeout in millis for both the connection timeout and the response read timeout. Note that the total timeout is effectively two times the given timeout.
     * @return the response time in milliseconds if the url could be pinged (otherwise <code>-1</code>) and the response code was between 200 and 399 (if not, this method returns the response code *
     *         -1)
     */
    public static int pingHttp(String url, int timeout) {
        // Otherwise an exception may be thrown on invalid SSL certificates:
        String httpUrl = url.replaceFirst("^https", "http");

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(httpUrl).openConnection();
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            connection.setRequestMethod("GET");
            long start = System.nanoTime();
            int responseCode = connection.getResponseCode();
            int responseTime = (int) TimeUtil.durationMillis(start);
            if (200 <= responseCode && responseCode <= 399) {
                log.debug("Successfully pinged {} in {} ms. Response code was {}", httpUrl, responseTime, responseCode);
                return responseTime;
            }
            else {
                log.debug("Unsuccessfully pinged {} in {} ms: Response code was: {}", httpUrl, responseTime, responseCode);
                return -responseCode;
            }

        }
        catch (IOException e) {
            log.debug("Unsuccessfully pinged " + httpUrl, e);
            return -1;
        }
    }

    public static int pingHttp(URL url, int timeout) {
        return pingHttp(url.toExternalForm(), timeout);
    }

    public static Document getDocument(URL url, Function<URL, Connection> connectionSetup) throws IOException {
        log.trace("Requesting HTML of {}", url);
        long start = System.nanoTime();
        Connection con = connectionSetup.apply(url);
        Document doc = con.get();
        log.debug("Retrieved HTML of {} in {} ms", url, TimeUtil.durationMillis(start));
        log.printf(Level.TRACE, "HTML of %s were:%n%s%n", url, doc);
        return doc;
    }
}

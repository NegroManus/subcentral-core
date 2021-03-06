package de.subcentral.core.metadata.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import de.subcentral.core.util.NetUtil;
import de.subcentral.core.util.Service.Status.Code;

public abstract class HttpMetadataService extends AbstractMetadataService {
    private static final Logger   log                = LogManager.getLogger(HttpMetadataService.class);

    /**
     * Default timeout: 10 seconds.
     */
    public static final int       DEFAULT_TIMEOUT    = 10_000;
    /**
     * Default test query: {@value #DEFAULT_TEST_QUERY}.
     */
    protected static final String DEFAULT_TEST_QUERY = "Game of Thrones S06E01";

    protected int                 timeout            = DEFAULT_TIMEOUT;

    // Metadata
    public String getHost() {
        return getSite().getLink();
    }

    public URL getHostUrl() {
        try {
            return new URL(getHost());
        }
        catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    // Config
    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    // Status
    @Override
    public Status checkStatus() {
        String testQuery = getTestQuery();
        Code code;
        long start = System.currentTimeMillis();
        try {
            List<?> results = search(testQuery, getSupportedRecordTypes().iterator().next());
            code = results.isEmpty() ? Code.LIMITED : Code.AVAILABLE;
        }
        catch (Exception e) {
            log.warn("Metadata database " + this + " failed to execute test query '" + testQuery + "'", e);
            code = Code.NOT_AVAILABLE;
        }
        long responseTime = System.currentTimeMillis() - start;
        return Status.of(code, responseTime);
    }

    protected String getTestQuery() {
        return DEFAULT_TEST_QUERY;
    }

    // Utility methods for child classes
    protected URL buildRelativeUrl(String path) throws IllegalArgumentException {
        return buildRelativeUrlImpl(path, null);
    }

    protected URL buildRelativeUrl(String queryKey, String queryValue) throws IllegalArgumentException {
        return buildRelativeUrlImpl(null, NetUtil.formatQueryString(queryKey, queryValue));
    }

    protected URL buildRelativeUrl(Map<String, String> queryKeyValuePairs) throws IllegalArgumentException {
        return buildRelativeUrlImpl(null, NetUtil.formatQueryString(queryKeyValuePairs));
    }

    protected URL buildRelativeUrl(String path, String queryKey, String queryValue) throws IllegalArgumentException {
        return buildRelativeUrlImpl(path, NetUtil.formatQueryString(queryKey, queryValue));
    }

    protected URL buildRelativeUrl(String path, Map<String, String> queryKeyValuePairs) throws IllegalArgumentException {
        return buildRelativeUrlImpl(path, NetUtil.formatQueryString(queryKeyValuePairs));
    }

    /**
     * 
     * @param path
     *            the path for the URL. Not null. Must start with "/"
     * @param query
     *            the query for the URL. Not null
     * @return An URL build of the host of this lookup and the given path and query
     * @throws URISyntaxException
     * @throws UnsupportedEncodingException
     * @throws MalformedURLException
     */
    private URL buildRelativeUrlImpl(String path, String query) throws IllegalArgumentException {
        try {
            URI uri = new URI(getHost());
            uri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), path, query, uri.getFragment());
            return uri.toURL();
        }
        catch (URISyntaxException | MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    protected Document getDocument(URL url) throws IOException {
        return NetUtil.getDocument(url, this::setupConnection);
    }

    /**
     * The default implementation sets up a connection with the timeout.
     * <p>
     * Subclasses may override this method, call super.setupConnection() and then set other configurations like cookies or user-agent info.
     * </p>
     * 
     * @param url
     *            thr url to connect to
     * @return a connection
     */
    protected Connection setupConnection(URL url) {
        // Because XRel.to blocks Java userAgents, we have to spoof it
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0";
        return Jsoup.connect(url.toExternalForm()).timeout(timeout).userAgent(userAgent);
    }
}

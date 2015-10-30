package de.subcentral.support.subcentralde;

import java.io.IOException;
import java.nio.file.Path;

import org.jsoup.nodes.Document;

public interface SubCentralApi
{

	public void login(String username, String password) throws IOException;

	/**
	 * @deprecated Currently not working (gets 404 because Cookies userID and password (hash) are missing)
	 */
	public void logout() throws IOException;

	public Document getContent(String url) throws IOException;

	public Path downloadAttachment(int attachmentId, Path directory) throws IOException;

}
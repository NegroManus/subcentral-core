package de.subcentral.mig;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.subcentral.core.util.StringUtil;

public class SubCentralDbApi
{
	private static final Logger	log	= LogManager.getLogger(SubCentralDbApi.class);

	private Connection			conn;

	public void connect(String url, String username, String password) throws SQLException
	{
		conn = DriverManager.getConnection(url, username, password);
		log.debug("Connected to {} as {}", url, username);
	}

	private void checkConnected()
	{
		if (conn == null)
		{
			throw new IllegalStateException("Not connected");
		}
	}

	public Post getFirstPost(int threadId) throws SQLException
	{
		checkConnected();
		try (PreparedStatement stmt = conn.prepareStatement("SELECT t.topic, p.message FROM wbb1_1_thread t, wbb1_1_post p WHERE t.threadID=? AND t.firstPostID=p.postID"))
		{
			stmt.setInt(1, threadId);
			try (ResultSet rs = stmt.executeQuery())
			{
				if (rs.next())
				{
					Post post = new Post();
					post.topic = rs.getString(1);
					Reader msgReader = rs.getCharacterStream(2);
					post.message = StringUtil.readerToString(msgReader);
					return post;
				}
			}
			catch (IOException e)
			{
				throw new SQLException(e);
			}
			return null;
		}
	}

	public Attachment getAttachment(int attachmentId) throws SQLException
	{
		checkConnected();
		try (PreparedStatement stmt = conn.prepareStatement("SELECT attachmentName, attachmentSize FROM wcf1_attachment WHERE attachmentID=?"))
		{
			stmt.setInt(1, attachmentId);
			try (ResultSet rs = stmt.executeQuery())
			{
				if (rs.next())
				{
					Attachment attachment = new Attachment();
					attachment.id = attachmentId;
					attachment.name = rs.getString(1);
					attachment.size = rs.getInt(2);
					return attachment;
				}
			}
		}
		return null;
	}

	public void disconnect() throws SQLException
	{
		if (conn != null)
		{
			conn.close();
			log.debug("Disconnected");
		}
	}

	public static class Post
	{
		private String	topic;
		private String	message;

		public String getTopic()
		{
			return topic;
		}

		public String getMessage()
		{
			return message;
		}
	}

	public static class Attachment
	{
		private int		id;
		private String	name;
		private int		size;

		public int getId()
		{
			return id;
		}

		public String getName()
		{
			return name;
		}

		public int getSize()
		{
			return size;
		}
	}
}

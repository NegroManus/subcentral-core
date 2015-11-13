package de.subcentral.mig;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import de.subcentral.core.util.StringUtil;

public class SubCentralBoardDbApi
{
	private Connection connection;

	public Connection getConnection()
	{
		return connection;
	}

	public void setConnection(Connection connection)
	{
		this.connection = connection;
	}

	private void checkConnected()
	{
		if (connection == null)
		{
			throw new IllegalStateException("Not connected");
		}
	}

	public Post getPost(int postId) throws SQLException
	{
		checkConnected();
		try (PreparedStatement stmt = connection.prepareStatement("SELECT subject, message FROM wbb1_1_post WHERE postID=?"))
		{
			stmt.setInt(1, postId);
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

	public Post getFirstPost(int threadId) throws SQLException
	{
		checkConnected();
		try (PreparedStatement stmt = connection.prepareStatement("SELECT t.topic, p.message FROM wbb1_1_thread t, wbb1_1_post p WHERE t.threadID=? AND t.firstPostID=p.postID"))
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
		try (PreparedStatement stmt = connection.prepareStatement("SELECT attachmentName, attachmentSize FROM wcf1_attachment WHERE attachmentID=?"))
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

package de.subcentral.mig.process;

import java.io.IOException;
import java.io.Reader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.util.StringUtil;

public class SubCentralBoard extends SubCentralDb
{
	public Post getPost(int postId) throws SQLException
	{
		checkConnected();
		try (PreparedStatement stmt = connection.prepareStatement("SELECT postID, subject, message FROM wbb1_1_post WHERE postID=?"))
		{
			stmt.setInt(1, postId);
			try (ResultSet rs = stmt.executeQuery())
			{
				if (rs.next())
				{
					Post post = new Post();
					post.id = rs.getInt(1);
					post.topic = rs.getString(2);
					Reader msgReader = rs.getCharacterStream(3);
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
					post.id = rs.getInt(1);
					post.topic = rs.getString(2);
					Reader msgReader = rs.getCharacterStream(3);
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

	public List<Thread> getChildThreadsByPrefix(int boardId, String prefix) throws SQLException
	{
		checkConnected();
		try (PreparedStatement stmt = connection.prepareStatement("SELECT threadID, topic FROM wbb1_1_thread WHERE boardID=? AND prefix=?"))
		{
			stmt.setInt(1, boardId);
			stmt.setString(2, prefix);
			ImmutableList.Builder<Thread> list = ImmutableList.builder();
			try (ResultSet rs = stmt.executeQuery())
			{
				while (rs.next())
				{
					Thread thread = new Thread();
					thread.id = rs.getInt(1);
					thread.topic = rs.getString(2);
					list.add(thread);
				}
			}
			return list.build();
		}
	}

	public static class Board
	{
		private int		id;
		private String	title;
		private String	description;

		public int getId()
		{
			return id;
		}

		public String getTitle()
		{
			return title;
		}

		public String getDescription()
		{
			return description;
		}
	}

	public static class Thread
	{
		private int		id;
		private String	topic;

		public int getId()
		{
			return id;
		}

		public String getTopic()
		{
			return topic;
		}
	}

	public static class Post
	{
		private int		id;
		private String	topic;
		private String	message;

		public int getId()
		{
			return id;
		}

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

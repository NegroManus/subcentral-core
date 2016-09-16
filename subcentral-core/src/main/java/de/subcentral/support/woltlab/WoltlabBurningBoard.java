package de.subcentral.support.woltlab;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.google.common.collect.ImmutableList;

public class WoltlabBurningBoard extends AbstractSqlApi {
	public WoltlabBurningBoard(Connection connection) {
		super(connection);
	}

	public WbbBoard getBoard(int boardId) throws SQLException {
		try (PreparedStatement stmt = connection.prepareStatement("SELECT title, description FROM wbb1_1_board WHERE boardID=?")) {
			stmt.setInt(1, boardId);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					WbbBoard board = new WbbBoard();
					board.id = boardId;
					board.title = rs.getString(1);
					board.description = rs.getString(2);
					return board;
				}
			}
			return null;
		}
	}

	public WbbThread getThread(int threadId) throws SQLException {
		try (PreparedStatement stmt = connection.prepareStatement("SELECT boardID, topic, prefix, firstPostID FROM wbb1_1_thread WHERE threadID=?")) {
			stmt.setInt(1, threadId);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					WbbThread thread = new WbbThread();
					thread.id = threadId;
					thread.boardId = rs.getInt(1);
					thread.topic = rs.getString(2);
					thread.prefix = rs.getString(3);
					thread.firstPostId = rs.getInt(4);
					return thread;
				}
			}
			return null;
		}
	}

	public WbbPost getPost(int postId) throws SQLException {
		try (PreparedStatement stmt = connection.prepareStatement("SELECT subject, message FROM wbb1_1_post WHERE postID=?")) {
			stmt.setInt(1, postId);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					WbbPost post = new WbbPost();
					post.id = postId;
					post.topic = rs.getString(1);
					post.message = rs.getString(2);
					return post;
				}
			}
			return null;
		}
	}

	public List<WbbBoard> getBoardsByParent(int boardId) throws SQLException {
		try (PreparedStatement stmt = connection.prepareStatement("SELECT boardID, title, description FROM wbb1_1_board WHERE parentID=?")) {
			stmt.setInt(1, boardId);
			try (ResultSet rs = stmt.executeQuery()) {
				ImmutableList.Builder<WbbBoard> boardList = ImmutableList.builder();
				while (rs.next()) {
					WbbBoard board = new WbbBoard();
					board.id = rs.getInt(1);
					board.title = rs.getString(2);
					board.description = rs.getString(2);
					boardList.add(board);
				}
				return boardList.build();
			}
		}
	}

	public List<WbbThread> getThreadsByBoard(int boardId) throws SQLException {
		try (PreparedStatement stmt = connection.prepareStatement("SELECT threadID, topic FROM wbb1_1_thread WHERE boardID=?")) {
			stmt.setInt(1, boardId);
			try (ResultSet rs = stmt.executeQuery()) {
				ImmutableList.Builder<WbbThread> threadList = ImmutableList.builder();
				while (rs.next()) {
					WbbThread thread = new WbbThread();
					thread.id = rs.getInt(1);
					thread.boardId = boardId;
					thread.topic = rs.getString(2);
					threadList.add(thread);
				}
				return threadList.build();
			}
		}
	}

	public List<WbbThread> getThreadsByPrefix(String prefix) throws SQLException {
		try (PreparedStatement stmt = connection.prepareStatement("SELECT threadID, boardID, topic FROM wbb1_1_thread WHERE prefix=?")) {
			stmt.setString(1, prefix);
			ImmutableList.Builder<WbbThread> list = ImmutableList.builder();
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					WbbThread thread = new WbbThread();
					thread.id = rs.getInt(1);
					thread.boardId = rs.getInt(2);
					thread.topic = rs.getString(3);
					thread.prefix = prefix;
					list.add(thread);
				}
			}
			return list.build();
		}
	}

	public List<WbbThread> getStickyThreads(int boardId) throws SQLException {
		try (PreparedStatement stmt = connection.prepareStatement("SELECT threadID, topic FROM wbb1_1_thread WHERE isSticky=1 AND boardID=?")) {
			stmt.setInt(1, boardId);
			ImmutableList.Builder<WbbThread> list = ImmutableList.builder();
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					WbbThread thread = new WbbThread();
					thread.id = rs.getInt(1);
					thread.boardId = boardId;
					thread.topic = rs.getString(2);
					thread.sticky = true;
					list.add(thread);
				}
			}
			return list.build();
		}
	}

	public WbbPost getFirstPost(int threadId) throws SQLException {
		try (PreparedStatement stmt = connection.prepareStatement("SELECT p.postID, t.topic, p.message FROM wbb1_1_thread t, wbb1_1_post p WHERE t.threadID=? AND t.firstPostID=p.postID")) {
			stmt.setInt(1, threadId);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					WbbPost post = new WbbPost();
					post.id = rs.getInt(1);
					post.topic = rs.getString(2);
					post.message = rs.getString(3);
					post.threadId = threadId;
					return post;
				}
			}
			return null;
		}
	}

	public WcfAttachment getAttachment(int attachmentId) throws SQLException {
		try (PreparedStatement stmt = connection.prepareStatement("SELECT attachmentName, attachmentSize FROM wcf1_attachment WHERE attachmentID=?")) {
			stmt.setInt(1, attachmentId);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					WcfAttachment attachment = new WcfAttachment();
					attachment.id = attachmentId;
					attachment.name = rs.getString(1);
					attachment.size = rs.getInt(2);
					return attachment;
				}
			}
		}
		return null;
	}

	public List<WcfAttachment> getAttachmentsByBoard(int boardId) throws SQLException {
		try (PreparedStatement stmt = connection.prepareStatement("SELECT a.attachmentID, a.attachmentName, a.attachmentSize" // line-break
				+ " FROM ((wcf1_attachment a JOIN wbb1_1_post p) JOIN wbb1_1_thread t)"
				+ " WHERE ((a.containerType = 'post') AND (a.containerID = p.postID) AND (p.threadID = t.threadID) AND (t.boardID = ?))")) {
			stmt.setInt(1, boardId);
			ImmutableList.Builder<WcfAttachment> list = ImmutableList.builder();
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					WcfAttachment att = new WcfAttachment();
					att.id = rs.getInt(1);
					att.name = rs.getString(2);
					att.size = rs.getInt(3);
					list.add(att);
				}
			}
			return list.build();
		}
	}

	public static class WbbBoard {
		private int		id;
		private String	title;
		private String	description;

		public int getId() {
			return id;
		}

		public String getTitle() {
			return title;
		}

		public String getDescription() {
			return description;
		}
	}

	public static class WbbThread {
		private int		id;
		private int		boardId;
		private String	topic;
		private String	prefix;
		private boolean	sticky;
		private int		firstPostId;

		public int getId() {
			return id;
		}

		public int getBoardId() {
			return boardId;
		}

		public String getTopic() {
			return topic;
		}

		public String getPrefix() {
			return prefix;
		}

		public boolean isSticky() {
			return sticky;
		}

		public int getFirstPostId() {
			return firstPostId;
		}
	}

	public static class WbbPost {
		private int		id;
		private String	topic;
		private String	message;
		private int		threadId;

		public int getId() {
			return id;
		}

		public String getTopic() {
			return topic;
		}

		public String getMessage() {
			return message;
		}

		public int getThreadId() {
			return threadId;
		}
	}

	public static class WcfAttachment {
		private int		id;
		private String	name;
		private int		size;

		public int getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public int getSize() {
			return size;
		}
	}
}

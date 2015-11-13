package de.subcentral.mig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.junit.Test;

public class DbPlayground
{
	private final String	url			= "jdbc:mysql://localhost:3306/sc_wbb316_dev";
	private final String	user		= "sc1_dev";
	private final String	password	= "xxx";

	@Test
	public void getConnection() throws SQLException
	{
		try (Connection conn = DriverManager.getConnection(url, user, password))
		{
			SubCentralBoardDbApi api = new SubCentralBoardDbApi();

			// Post post = api.getFirstPost(36734);
			// System.out.println(post.getTopic());
			// System.out.println(post.getMessage());

			api.getAttachment(182270);
		}
	}
}

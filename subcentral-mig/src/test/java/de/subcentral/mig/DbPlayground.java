package de.subcentral.mig;

import java.sql.SQLException;

import org.junit.Test;

public class DbPlayground
{
	private final String	url			= "jdbc:mysql://localhost:3306/sc_wbb316_dev";
	private final String	username	= "sc1_dev";
	private final String	password	= "xxx";

	@Test
	public void getConnection() throws SQLException
	{
		SubCentralDbApi api = new SubCentralDbApi();
		api.connect(url, username, password);

		// Post post = api.getFirstPost(36734);
		// System.out.println(post.getTopic());
		// System.out.println(post.getMessage());

		api.getAttachment(182270);

		api.disconnect();
	}
}

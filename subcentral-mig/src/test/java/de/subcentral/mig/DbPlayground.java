package de.subcentral.mig;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.Test;

import de.subcentral.support.woltlab.WoltlabBurningBoard;
import de.subcentral.support.woltlab.WoltlabBurningBoard.WbbBoard;

public class DbPlayground
{
	@Test
	public void getConnection() throws SQLException, ConfigurationException
	{
		try (Connection conn = MigTestUtil.connect())
		{
			WoltlabBurningBoard api = new WoltlabBurningBoard(conn);

			// WbbPost post = api.getFirstPost(36734);
			// System.out.println(post.getTopic());
			// System.out.println(post.getMessage());

			System.out.println("Aktuelle Serien");
			for (WbbBoard b : api.getBoardsByParent(160))
			{
				System.out.println(b.getTitle());
			}
			for (WbbBoard b : api.getBoardsByParent(170))
			{
				System.out.println(b.getTitle());
			}
		}
	}
}

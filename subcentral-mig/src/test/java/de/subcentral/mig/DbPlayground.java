package de.subcentral.mig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.junit.Test;

public class DbPlayground
{
	private final String	userName	= "sc1_dev";
	private final String	password	= "xxx";
	private final String	url			= "jdbc:mysql://localhost:3306/sc_wbb316_dev";

	@Test
	public void getConnection() throws SQLException
	{
		Connection conn = DriverManager.getConnection(this.url, userName, password);

		System.out.println("Connected to database: " + conn);
	}
}

package de.subcentral.mig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.junit.Test;

public class DbPlayground
{
	private final String	userName	= "xxx";
	private final String	password	= "xxx";
	private final String	url			= "xxx";

	@Test
	public Connection getConnection() throws SQLException
	{

		Connection conn = null;
		Properties connectionProps = new Properties();
		connectionProps.put("user", this.userName);
		connectionProps.put("password", this.password);

		conn = DriverManager.getConnection(this.url, connectionProps);

		System.out.println("Connected to database");
		return conn;
	}
}

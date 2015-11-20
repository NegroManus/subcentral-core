package de.subcentral.support.woltlab;

import java.sql.Connection;
import java.sql.SQLException;

public class AbstractDatabaseApi
{
	protected Connection connection;

	public Connection getConnection()
	{
		return connection;
	}

	public void setConnection(Connection connection)
	{
		this.connection = connection;
	}

	protected void checkConnected()
	{
		if (connection == null)
		{
			throw new IllegalStateException("Not connected");
		}
	}

	protected void checkUpdated(Object updateObj, int affectedRows) throws SQLException
	{
		if (affectedRows == 0)
		{
			throw new SQLException("Update of " + updateObj + " failed, no rows affected.");
		}
	}
}

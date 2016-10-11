package de.subcentral.support.woltlab;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

public class AbstractSqlApi {
    protected final Connection connection;

    protected AbstractSqlApi(Connection connection) {
        this.connection = Objects.requireNonNull(connection, "connection");
    }

    public Connection getConnection() {
        return connection;
    }

    protected void checkUpdated(Object updateObj, int affectedRows) throws SQLException {
        if (affectedRows == 0) {
            throw new SQLException("Update of " + updateObj + " failed, no rows affected.");
        }
    }
}

package net.oldschoolminecraft.bcord.util;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class MySQLConnectionManager
{
	private final ComboPooledDataSource cpds = new ComboPooledDataSource();

	public MySQLConnectionManager(String url, String user, String password)
	{
		try
		{
			cpds.setDriverClass("com.mysql.cj.jdbc.Driver");
			cpds.setJdbcUrl(url);
			cpds.setUser(user);
			cpds.setPassword(password);
			cpds.setMaxPoolSize(50);
			cpds.setMaxStatementsPerConnection(5);
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
	}

	public Connection getConnection() throws SQLException
	{
		Connection conn = cpds.getConnection();
		if (isValid(conn) && conn.isValid(5000))
			return conn;
		return cpds.getConnection();
	}

	private boolean isValid(Connection connection) {
		try {
			return connection != null && !connection.isClosed();
		} catch (SQLException e) {
			e.printStackTrace(System.err);
			return false;
		}
	}
}

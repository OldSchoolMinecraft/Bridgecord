package net.oldschoolminecraft.bcord.util;

import org.apache.commons.dbcp.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class MySQLConnectionManager
{
	private final BasicDataSource bds = new BasicDataSource();

	public MySQLConnectionManager(String url, String user, String password)
	{
		try
		{
			bds.setDriverClassName("com.mysql.cj.jdbc.Driver");
			bds.setUrl(url);
			bds.setUsername(user);
			bds.setPassword(password);
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
	}

	public Connection getConnection() throws SQLException
	{
		Connection conn = bds.getConnection();
		if (isValid(conn) && conn.isValid(0))
			return conn;
		return bds.getConnection();
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

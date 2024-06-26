package net.oldschoolminecraft.bcord.util;

import java.sql.Connection;
import java.sql.SQLException;

public class MySQLConnectionManager
{
	private SimpleConnectionPool scp;

	public MySQLConnectionManager(String url, String user, String password)
	{
		try
		{
			scp = SimpleConnectionPool.create(url, user, password, 10);

//			bds.setLogWriter(new PrintWriter(System.out));
//			bds.setDriverClassName("com.mysql.cj.jdbc.Driver");
//			bds.setUrl(url);
//			bds.setUsername(user);
//			bds.setPassword(password);
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
	}

	public Connection getConnection() throws SQLException
	{
		Connection conn = scp.getConnection();
		if (isValid(conn) && conn.isValid(0))
			return conn;
		return scp.getConnection();
	}

	public void shutdown() throws SQLException
	{
		scp.shutdown();
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

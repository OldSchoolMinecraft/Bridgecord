package net.oldschoolminecraft.bcord.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MySQLConnectionPool {
	private static final int MAX_POOL_SIZE = 10;
	private static final long CONNECTION_TIMEOUT = 5000; // Timeout in milliseconds
	private final String URL;
	private final String USERNAME;
	private final String PASSWORD;

	private List<Connection> connectionPool;

	public MySQLConnectionPool(String url, String user, String password)
	{
		this.URL = url;
		this.USERNAME = user;
		this.PASSWORD = password;
		initializeConnectionPool();
	}

	private void initializeConnectionPool() {
		connectionPool = new ArrayList<>(MAX_POOL_SIZE);
		try {
			for (int i = 0; i < MAX_POOL_SIZE; i++) {
				Connection connection = createConnectionWithTimeout(CONNECTION_TIMEOUT);
				if (connection != null) {
					connectionPool.add(connection);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			// Handle exception (log, throw, etc.)
		}
	}

	private Connection createConnectionWithTimeout(long timeout) throws SQLException {
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
		} catch (SQLException e) {
			// Handle connection creation failure
			e.printStackTrace();
			// Handle exception (log, throw, etc.)
			throw e;
		}

		if (!isValid(connection)) {
			closeConnection(connection);
			connection = null;
			// Handle invalid connection
		}

		return connection;
	}

	private boolean isValid(Connection connection) {
		try {
			return connection != null && !connection.isClosed();
		} catch (SQLException e) {
			e.printStackTrace();
			// Handle exception (log, throw, etc.)
			return false;
		}
	}

	public synchronized Connection getConnection() {
		Connection connection = null;
		long startTime = System.currentTimeMillis();

		while (System.currentTimeMillis() - startTime < CONNECTION_TIMEOUT) {
			if (!connectionPool.isEmpty()) {
				connection = connectionPool.remove(connectionPool.size() - 1);
				if (isValid(connection)) {
					return connection;
				}
			} else {
				try {
					TimeUnit.MILLISECONDS.sleep(100); // Wait before retrying
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					// Handle interruption
				}
			}
		}
		// Handle timeout
		return null;
	}

	public synchronized void releaseConnection(Connection connection) {
		if (connection != null) {
			if (isValid(connection)) {
				connectionPool.add(connection);
			} else {
				closeConnection(connection);
			}
		}
	}

	public synchronized void closeConnections() {
		for (Connection connection : connectionPool) {
			closeConnection(connection);
		}
		connectionPool.clear();
	}

	private void closeConnection(Connection connection) {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			// Handle exception (log, throw, etc.)
		}
	}
}

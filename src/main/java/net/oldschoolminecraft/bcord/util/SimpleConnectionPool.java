package net.oldschoolminecraft.bcord.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SimpleConnectionPool implements ConnectionPool
{
    private final String url;
    private final String user;
    private final String password;
    private final List<Connection> connectionPool;
    private final List<Connection> usedConnections = new ArrayList<>();
    private int MAX_POOL_SIZE = 25; // 25 connections
    private int MAX_TIMEOUT = 10000; // 10 seconds

    public static SimpleConnectionPool create(String url, String user, String password) throws SQLException
    {
        return create(url, user, password, 10);
    }

    public static SimpleConnectionPool create(String url, String user, String password, int INITIAL_POOL_SIZE) throws SQLException
    {
        List<Connection> pool = new ArrayList<>(INITIAL_POOL_SIZE);
        for (int i = 0; i < INITIAL_POOL_SIZE; i++)
            pool.add(createConnection(url, user, password));
        return new SimpleConnectionPool(url, user, password, pool);
    }

    public SimpleConnectionPool setMaxConnections(int max)
    {
        MAX_POOL_SIZE = max;
        return this;
    }

    public SimpleConnectionPool setMaxTimeout(int max)
    {
        MAX_TIMEOUT = max;
        return this;
    }

    public SimpleConnectionPool(String url, String user, String password, List<Connection> pool)
    {
        this.url = url;
        this.user = user;
        this.password = password;
        this.connectionPool = pool;
    }

    @Override
    public Connection getConnection() throws SQLException
    {
        if (connectionPool.isEmpty())
        {
            if (usedConnections.size() < MAX_POOL_SIZE) connectionPool.add(createConnection(url, user, password));
            else throw new RuntimeException("Maximum pool size reached, no available connections!");
        }

        Connection connection = connectionPool.remove(connectionPool.size() - 1);
        if (!connection.isValid(MAX_TIMEOUT))
            connection = createConnection(url, user, password);
        usedConnections.add(connection);
        return connection;
    }

    @Override
    public boolean releaseConnection(Connection connection)
    {
        connectionPool.add(connection);
        return usedConnections.remove(connection);
    }

    private static Connection createConnection(String url, String user, String password) throws SQLException
    {
        return DriverManager.getConnection(url, user, password);
    }

    public void shutdown() throws SQLException
    {
        usedConnections.forEach(this::releaseConnection);
        for (Connection c : connectionPool) c.close();
        connectionPool.clear();
    }

    public int getSize()
    {
        return connectionPool.size() + usedConnections.size();
    }

    public String getUrl()
    {
        return url;
    }

    public String getUser()
    {
        return user;
    }

    public String getPassword()
    {
        return password;
    }
}
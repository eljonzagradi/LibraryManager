package models;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Database {

	static Connection connection = null;

    static String url = "jdbc:mysql://localhost:3306/eljonzagradi";
    static String user = "root";
    static String password = "root";

    public static Connection connect() {
        Connection connection = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return connection;
    }

	public static void disconnect() throws SQLException {
		try {
			if (connection != null && !connection.isClosed()) {
				connection.close();
			}
		} catch (Exception e) {
			throw e;
		}
	}

    public static PreparedStatement statement(String SqlQuery) {
        PreparedStatement statement = null;
        try {
            statement = connect().prepareStatement(SqlQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statement;
    }

}
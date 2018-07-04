package com.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection
{
	private static String username = "food";
	private static String password = "food";
	private static String databaseURL = "jdbc:mysql://localhost:3306/food";
	
	// method for setting up the connection to our MySQL database
	public static Connection getConnection() throws SQLException
	{
		Connection myConnection = DriverManager.getConnection(databaseURL, username, password);
		return myConnection;
	}
	
	
}

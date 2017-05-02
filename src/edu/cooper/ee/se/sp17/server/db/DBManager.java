package edu.cooper.ee.se.sp17.server.db;

import java.sql.*;

public class DBManager {

	private final static String DB_USER = "set_server";
	private final static String DB_PASS = "sable";
	private final static String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	private final static String DB_URL = "jdbc:mysql://sable08.ee.cooper.edu/set_server?autoReconnect=true";

	private static Connection conn = null;

	public static void init() {
		try{
			Class.forName(JDBC_DRIVER);
			conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public static ResultSet exec(String q) {
		try{
			Statement stmt = conn.createStatement();
			boolean res = stmt.execute(q);
			return stmt.getResultSet();
		}catch(SQLException e){
			e.printStackTrace();
		}
		
		return null;
	}

	public static void close() {
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static Connection getConnection() {
		return conn;
	}

	// Extra helper functions for set server
	public static int login(String u, String p) {
		if (conn == null) {
			init();
		}

		ResultSet rc = exec("SELECT id FROM users WHERE username = \"" + u + "\" AND password = MD5(\"" + p + "\");");

		try {
			if (rc == null || !rc.next()) // No such user found
				return 0;
			int uid = rc.getInt("id"); // Success
			rc.close();
			return uid;
		} catch (SQLException e) {
			e.printStackTrace();
			return -1; // Error
		}
	}

	public static int register(String u, String p) {
		if (conn == null) {
			init();
		}

		ResultSet rc = exec("SELECT id FROM users WHERE username = \"" + u + "\";");

		try {
			if (rc.next())
				return 0; // Exists
			rc.close();
			rc = exec("INSERT INTO `users` (`id`,`username`,`password`) VALUES (NULL,'"+u+"',MD5('"+p+"'));");
			return 1; // Success
		} catch (SQLException e) {
			e.printStackTrace();
			return -1; // Error
		}
	}
}
package xf.ad.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Tools {
	//192.168.8.33
	private static final String URL_STRING = "jdbc:oracle:thin:@192.168.8.33:1521:orcl"; // 地址
	private static final String USER_STRING = "HSAD"; // 用户名
	private static final String PW_STRING = "QDHXKJstats"; // 密码

	public Tools() {
		super();
		// TODO Auto-generated constructor stub
	}

	public static Connection createConnection() {
		Connection connection = null;
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			connection = DriverManager.getConnection(URL_STRING, USER_STRING,PW_STRING);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return connection;
	}
}

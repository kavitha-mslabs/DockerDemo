package tneb.ccms.admin.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataBaseUtil {

	private static Logger logger = LoggerFactory.getLogger(DataBaseUtil.class.getName());

	public static Connection connectOracleDataBase() throws Exception {
		PropertiesUtil propertiesUtil = new PropertiesUtil();
		Connection connection = null;

		try {
			Class.forName(propertiesUtil.DB_DRIVER);
			connection = DriverManager.getConnection(propertiesUtil.DB_URL, propertiesUtil.DB_USER_NAME,
					propertiesUtil.DB_PASSWORD);
		} catch (Exception e) {
			System.out.println("Connection Failed! connectOracleDataBase");
			logger.error(ExceptionUtils.getStackTrace(e));
			throw new Exception("Unable to connect to oracle data base");
		}
		return connection;
	}
	

	public static String getSequenceValue(Connection connection, String sequenceName) throws Exception {
		String sequenceValue = "";
		String query = "SELECT " + sequenceName + ".nextval as seq FROM dual";
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				sequenceValue = resultSet.getString("seq");
			}
		} catch (Exception e) {
			System.out.println("Connection Failed! getSequenceValue");
			logger.error(ExceptionUtils.getStackTrace(e));
			throw e;
		} finally {
			if (statement != null) {
				statement.close();
			}
			if (resultSet != null) {
				resultSet.close();
			}
		}

		return sequenceValue;
	}

}

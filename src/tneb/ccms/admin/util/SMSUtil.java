package tneb.ccms.admin.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SMSUtil {

	private static Logger logger = LoggerFactory.getLogger(SMSUtil.class.getName());

	public static String sendSMS(String otp, String mobile, String message) {
		Statement statement = null;
		Connection connection = null;
		String sequeneValue = null;
		try {
			
			if(otp == null) {
				otp = "";
			}
			
			connection = DataBaseUtil.connectOracleDataBase();
			sequeneValue = DataBaseUtil.getSequenceValue(connection, "SMSGS_ID_SEQ");
			String insertQuery = "INSERT INTO SMSGS(ID, OTP, CUSCODE, ENTRYDT, MOBILENO, MSG) VALUES " + 
							"(" + sequeneValue	+ ", '" + otp + "','" + mobile + "', SYSDATE , '" + mobile + "','" + message + "')";
			statement = connection.createStatement();
			statement.execute(insertQuery);
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			e.printStackTrace();
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (Exception e) {
				logger.error(ExceptionUtils.getStackTrace(e));
			}
		}
		return sequeneValue;
	}
	
	
	public static String getLatestRegisterOtpByMobile(String mobile) {
		Statement statement = null;
		Connection connection = null;
		String otp = null;
		ResultSet resultSet = null;
		try {
			connection = DataBaseUtil.connectOracleDataBase();
			String sql = "select otp from smsgs where rownum = 1 and mobileno = '"+mobile+"' order by id desc ";
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				otp = resultSet.getString("otp");
			}
			
		} catch (Exception e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			e.printStackTrace();
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (resultSet != null) {
					resultSet.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				logger.error(ExceptionUtils.getStackTrace(e));
				e.printStackTrace();
			}
		}
		return otp;
	}
}

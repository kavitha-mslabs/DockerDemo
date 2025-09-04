
package tneb.ccms.admin.util;

import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

public class SmsClient {

	public static boolean sendSms(final String id) {
		PropertiesUtil propertiesUtil = new PropertiesUtil();
		final String regval = "SendOTP";
		final String url = propertiesUtil.SMS_WSDL_URL;
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			connection = getConnectionCCMapp(propertiesUtil);
			final SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
			final SOAPConnection soapConnection = soapConnectionFactory.createConnection();
			final String sql = "select count(*) cnt from smsgs where id = " + id + " and sms_sent is null";
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
			Integer cnt = null;
			while (resultSet.next()) {
				cnt = Integer.parseInt(resultSet.getString("CNT"));
			}
			if (cnt > 0) {
				CompletableFuture.runAsync(() -> {
					try {
						soapConnection.call(createSOAPRequest(regval, id, propertiesUtil), (Object) url);
					}  catch (Exception e) {
						Logger.getLogger(SmsClient.class.getName()).log(Level.SEVERE, null, e);
					}
				    });
			}
		} catch (Exception ex) {
			Logger.getLogger(SmsClient.class.getName()).log(Level.SEVERE, null, ex);
			return false;
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
			} catch (Exception e) {
				Logger.getLogger(SmsClient.class.getName()).log(Level.SEVERE, null, e);
			}
		}
		return true;
	}

	private static SOAPMessage createSOAPRequest(final String regcode, final String id, PropertiesUtil propertiesUtil) throws Exception {
		try {
			final MessageFactory messageFactory = MessageFactory.newInstance();
			final SOAPMessage soapMessage = messageFactory.createMessage();
			final SOAPPart soapPart = soapMessage.getSOAPPart();
			final String serverURI = propertiesUtil.SMS_SERVER_URL;
			final SOAPEnvelope envelope = soapPart.getEnvelope();
			envelope.addNamespaceDeclaration("sms", serverURI);
			final SOAPBody soapBody = envelope.getBody();
			final SOAPElement soapBodyElem_master = soapBody.addChildElement(regcode, "sms");
			final SOAPElement soapBodyElem_ut0 = soapBodyElem_master.addChildElement("uname");
			soapBodyElem_ut0.addTextNode(propertiesUtil.SMS_USER_NAME);
			final SOAPElement soapBodyElem_ut2 = soapBodyElem_master.addChildElement("password");
			soapBodyElem_ut2.addTextNode(propertiesUtil.SMS_PASSWORD);
			final SOAPElement soapBodyElem_ut3 = soapBodyElem_master.addChildElement("id");
			soapBodyElem_ut3.addTextNode(id);
			final MimeHeaders headers = soapMessage.getMimeHeaders();
			headers.addHeader("SOAPAction", serverURI + "sms");
			soapMessage.saveChanges();
			soapMessage.writeTo((OutputStream) System.out);
			return soapMessage;
		} catch (SOAPException ex) {
			Logger.getLogger(SmsClient.class.getName()).log(Level.SEVERE, null, (Throwable) ex);
			ex.printStackTrace();
			return null;
		}
	}

	private static Connection getConnectionCCMapp(PropertiesUtil propertiesUtil) {
		Connection conn = null;
		try {
			Class.forName(propertiesUtil.DB_DRIVER);
			conn = DriverManager.getConnection(propertiesUtil.DB_URL, propertiesUtil.DB_USER_NAME,
					propertiesUtil.DB_PASSWORD);
		} catch (Exception e) {
			Logger.getLogger(SmsClient.class.getName()).log(Level.SEVERE, null, (Throwable) e);
			e.printStackTrace();
		}
		return conn;
	}
}

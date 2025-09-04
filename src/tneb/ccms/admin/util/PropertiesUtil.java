package tneb.ccms.admin.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtil {

	Properties prop = System.getProperties();

	public PropertiesUtil() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			InputStream input = classLoader.getResourceAsStream("application.properties");
			prop.load(input);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public final String IMAGE_BASE_URL = prop.getProperty("image.url");

	public final String GET_SECTION_FROM_GIS = prop.getProperty("find.gis");
	
	public final String FIND_CONSUMER = prop.getProperty("find.consumer");

	public final String SMS_WSDL_URL = prop.getProperty("wsdl.url");
	
	public final String SMS_SERVER_URL = prop.getProperty("server.url");
	
	public final String SMS_USER_NAME = prop.getProperty("sms.username");
	
	public final String SMS_PASSWORD = prop.getProperty("sms.password");
	
	public final String DB_DRIVER = prop.getProperty("connection.driver");
	
	public final String DB_URL = prop.getProperty("connection.url");
	
	public final String DB_USER_NAME = prop.getProperty("connection.username");
	
	public final String DB_PASSWORD = prop.getProperty("connection.password");
}

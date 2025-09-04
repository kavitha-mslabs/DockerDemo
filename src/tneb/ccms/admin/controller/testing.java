package tneb.ccms.admin.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class testing {

	public testing() {
		super();
		init();

	}

	private void init() {
		call();
	}
public void call()
{
	System.err.println("welcome");
	 String source = "api"; // example source
    try
    {
    
    	 URL url = new URL("http://localhost:8080/api/users"); // your endpoint
         HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    
    String phone_number= "7550392550";
    Integer complaint_id = 122;
    conn.setRequestMethod("POST");
    conn.setRequestProperty("Content-Type", "application/json; utf-8");
    conn.setDoOutput(true); // this enables writing to the request body

    String jsonInputString = "{\"name\": \"John\", \"email\": \"john@example.com\"}";

    try (OutputStream os = conn.getOutputStream()) {
        byte[] input = jsonInputString.getBytes("utf-8");
        os.write(input, 0, input.length);
    }

    int responseCode = conn.getResponseCode();
    System.out.println("Response Code: " + responseCode);
    
  
} catch (IOException e) {
  System.err.println("Error calling Doocti API: " + e.getMessage());
  e.printStackTrace();

	
}

}
}

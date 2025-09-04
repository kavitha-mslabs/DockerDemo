package tneb.ccms.admin.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.simple.JSONObject;

public class Network {

	PropertiesUtil propertiesUtil = new PropertiesUtil();
	
	@SuppressWarnings("unchecked")
	public String restOperation(JSONObject objIP, String url) {
		JSONObject json = new JSONObject();
		String output = "ERROR";
		try {

			URL object = new URL(url);

			HttpURLConnection con = (HttpURLConnection) object.openConnection();
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestMethod("POST");

			// String input = consumer_no;

			OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
			wr.write(objIP.toJSONString());
			// System.out.println(" OPT : "+ input);
			wr.flush();

			if (con.getResponseCode() != 200) {
				output = "Failed : HTTP error code : " + con.getResponseCode();
				json.put("error", 1);

				json.put("message", con.getResponseCode() + con.getResponseMessage());
				output = json.toString();

			}

			BufferedReader br = new BufferedReader(new InputStreamReader((con.getInputStream())));

			String opt = null;
			System.out.println("Output from Server .... \n");
			while ((opt = br.readLine()) != null) {
				if (opt != null)
					output = opt;
			}

			con.disconnect();

		} catch (MalformedURLException e) {
			System.out.println("ERROR MALURL : " + e.getMessage());
			output = "ERROR MAL URL : " + e.getMessage();

		} catch (IOException e) {

			System.out.println("ERROR IO : " + e.getMessage());
			output = "ERROR IO: " + e.getMessage();

		}
		System.out.println(output);
		return output;

	}

	@SuppressWarnings("unchecked")
	public String findConsumer(String consumerNo) {
		String output = "ERROR";
		try {
			System.out.println("Going to find consumer :::    "+consumerNo+"    In the URL :::::   "+propertiesUtil.FIND_CONSUMER);
			
			URL object = new URL(propertiesUtil.FIND_CONSUMER);
			
			JSONObject consumer = new JSONObject();
			consumer.put("consumer_no", consumerNo);

			HttpURLConnection con = (HttpURLConnection) object.openConnection();
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestMethod("POST");

			OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
			wr.write(consumer.toJSONString());
			// System.out.println(" OPT : "+ input);
			wr.flush();

			if (con.getResponseCode() != 200) {
				output = "Failed : HTTP error code : " + con.getResponseCode();
				throw new RuntimeException(
						"Failed : HTTP error code : " + con.getResponseCode() + con.getResponseMessage());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((con.getInputStream())));

			String opt = null;
			System.out.println("Output from Server .... \n");
			while ((opt = br.readLine()) != null) {
				if (opt != null) {
					output = opt;
				}
			}

			con.disconnect();

		} catch (MalformedURLException e) {
			System.out.println("ERROR MALURL : " + e.getMessage());
			output = "ERROR MAL URL : " + e.getMessage();

		} catch (IOException e) {

			System.out.println("ERROR IO : " + e.getMessage());
			output = "ERROR IO: " + e.getMessage();

		}
		System.out.println(output);
		return output;

	}

}

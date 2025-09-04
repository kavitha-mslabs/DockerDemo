package tneb.ccms.admin.controller;


import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.StringEntity;
import org.json.simple.JSONObject;

import java.nio.charset.StandardCharsets;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
public class FeedbackCaller {

   //private static final String FEEDBACK_API_URL = "http://localhost:8080/tangedco-admin/api/feedback"; 
    private static final String FEEDBACK_API_URL = "https://ccms.tangedco.org/tangedco-admin/api/feedback"; 
    
    // 🔁 adjust if needed
    private static final String AUTH_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjbGllbnQiOiJBZG1pbiIsInRlbmFudF9pZCI6ImN0aV90YW5nZWRjb18yMDI0MTExMyIsInNjb3BlIjoiYXBpLXYyIiwiaXNzIjoiaHR0cHM6Ly9saXZlLWRvb2N0aS5hdS5hdXRoMC5jb20vIiwic3ViIjoiejZMMzlyekRhZmtub0tucjVWUHloMU8zRW9aVUxGUGRAY2xpZW50cyIsImF1ZCI6Imh0dHBzOi8vcHJvZC1hcGkuZG9vY3RpLmNvbSIsImV4cCI6MTgwODY0OTc5MTE3MCwiaWF0IjoxNzQ1NTc3NzkxfQ.62pbM0VJL5YgZ6MSZsZJGTpcvaGQdkpwWCkpXAx6N0E"; // ✅ your full token

    public static void sendFeedback(String contactNo, Integer complaintId) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(FEEDBACK_API_URL);

            // ✅ Prepare parameters
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("contactNo", contactNo));
            params.add(new BasicNameValuePair("complaint_id", String.valueOf(complaintId)));
            System.err.println("Sending contactNo = " + contactNo);
            System.err.println("Sending complaint_id = " + complaintId);

            // ✅ Set headers
            httpPost.setHeader("Authorization", AUTH_TOKEN);
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded"); // crucial!

            // ✅ Set body
            httpPost.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

            // ✅ Execute request
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                String result = EntityUtils.toString(response.getEntity());
                System.out.println("Feedback Servlet Response: " + result);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
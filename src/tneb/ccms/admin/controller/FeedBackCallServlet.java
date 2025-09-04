package tneb.ccms.admin.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

@WebServlet("/api/feedback")
public class FeedBackCallServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String EXPECTED_AUTH_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjbGllbnQiOiJBZG1pbiIsInRlbmFudF9pZCI6ImN0aV90YW5nZWRjb18yMDI0MTExMyIsInNjb3BlIjoiYXBpLXYyIiwiaXNzIjoiaHR0cHM6Ly9saXZlLWRvb2N0aS5hdS5hdXRoMC5jb20vIiwic3ViIjoiejZMMzlyekRhZmtub0tucjVWUHloMU8zRW9aVUxGUGRAY2xpZW50cyIsImF1ZCI6Imh0dHBzOi8vcHJvZC1hcGkuZG9vY3RpLmNvbSIsImV4cCI6MTgwODY0OTc5MTE3MCwiaWF0IjoxNzQ1NTc3NzkxfQ.62pbM0VJL5YgZ6MSZsZJGTpcvaGQdkpwWCkpXAx6N0E";
    private static final String DOOCTI_API_URL = "https://dialer-tangedco.doocti.com/api";
    private static final String LIST_ID = "1";
    private static final String CHANNEL = "lead";

    public FeedBackCallServlet() {
        super();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
       // response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        JSONObject jsonResponse = new JSONObject();

        // Validate Authorization
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.equals(EXPECTED_AUTH_TOKEN)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Invalid or missing authorization token");
            out.print(jsonResponse.toString());
            return;
        }

        try {
            // Normally extracted from request; hardcoded here for testing/demo
//            String phoneNumber = "7550392550";
            //String source = "api";
            //String phoneNumber = "8754612823";
            //String complaintIdStr = "1";

             String source = "api";
            String phoneNumber = request.getParameter("contactNo");
            String complaintIdStr = request.getParameter("complaint_id");
            
            // Validate parameters
            if (phoneNumber == null || complaintIdStr == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Missing required parameters");
                out.print(jsonResponse.toString());
                return;
            }
            
            int complaintId = Integer.parseInt(complaintIdStr);
            
            System.err.println("Received contactNo = " + phoneNumber);
            System.err.println("Received complaint_id = " + complaintIdStr);

            
            JSONObject dooctiResponse = callDooctiApi(phoneNumber, source, complaintId);

            JSONObject fields = new JSONObject();
            fields.put("phone_number", phoneNumber);
            fields.put("source", source);
            fields.put("complaint_id", complaintId);
            //fields.put("doocti_response", dooctiResponse);

            JSONObject finalResponse = new JSONObject();
            finalResponse.put("fields", fields);

            response.setStatus(HttpServletResponse.SC_OK);
            out.print(finalResponse.toString());

        } catch (Exception e) {
            // You could replace this with real logging
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Error processing request: " + e.getMessage());
            out.print(jsonResponse.toString());
        }
    }

    @SuppressWarnings("unchecked")
    private JSONObject callDooctiApi(String phoneNumber, String source, int complaintId) throws Exception {
        String apiUrl = DOOCTI_API_URL + "?list_id=" + LIST_ID + "&channel=" + CHANNEL;

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(apiUrl);

            JSONObject fieldsObject = new JSONObject();
            fieldsObject.put("phone_number", phoneNumber);
            fieldsObject.put("source", source);
            fieldsObject.put("complaint_id", complaintId);

            JSONObject requestBody = new JSONObject();
            requestBody.put("fields", fieldsObject);

            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + EXPECTED_AUTH_TOKEN);
            httpPost.setEntity(new StringEntity(requestBody.toString(), StandardCharsets.UTF_8));

            try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost)) {
                HttpEntity entity = httpResponse.getEntity();
                String responseString = EntityUtils.toString(entity, StandardCharsets.UTF_8);

                JSONParser parser = new JSONParser();
                return (JSONObject) parser.parse(responseString);
            }
        }
    }
}

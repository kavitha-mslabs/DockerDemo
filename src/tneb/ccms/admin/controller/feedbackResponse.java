package tneb.ccms.admin.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

import org.json.simple.parser.JSONParser;
@WebServlet("/api/feedback/response")
public class feedbackResponse extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public feedbackResponse() {
        super();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String token = request.getHeader("Authorization");
        PrintWriter out = response.getWriter();
        JSONObject jsonResponse = new JSONObject();
        if (token == null || token.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            jsonResponse.put("message", "Authorization header is missing");
            out.write(jsonResponse.toString());
            return;
        }

        if (!"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjbGllbnQiOiJBZG1pbiIsInRlbmFudF9pZCI6ImN0aV90YW5nZWRjb18yMDI0MTExMyIsInNjb3BlIjoiYXBpLXYyIiwiaXNzIjoiaHR0cHM6Ly9saXZlLWRvb2N0aS5hdS5hdXRoMC5jb20vIiwic3ViIjoiejZMMzlyekRhZmtub0tucjVWUHloMU8zRW9aVUxGUGRAY2xpZW50cyIsImF1ZCI6Imh0dHBzOi8vcHJvZC1hcGkuZG9vY3RpLmNvbSIsImV4cCI6MTgwODY0OTc5MTE3MCwiaWF0IjoxNzQ1NTc3NzkxfQ.62pbM0VJL5YgZ6MSZsZJGTpcvaGQdkpwWCkpXAx6N0E".equals(token)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
            return;
        }

        StringBuilder jsonBuffer = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuffer.append(line);
            }
        }

        Gson gson = new Gson();
        FeedbackResponseValueBean input = gson.fromJson(jsonBuffer.toString(), FeedbackResponseValueBean.class);

        if (input.getComplaint_id() == null || input.getComplaint_id().trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
            jsonResponse.put("message", "Complaint ID is required");
            out.write(jsonResponse.toString());
            return;
        }

     
        if (input.getFeedback_datetime() == null || input.getFeedback_datetime().trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
            jsonResponse.put("message", "Feedback_datetime is required");
            out.write(jsonResponse.toString());
            return;
        }
        try {
        	 Feedback feedbackDao = new Feedback();
             feedbackDao.save(input);
            response.setStatus(HttpServletResponse.SC_OK); // 200
            jsonResponse.put("message", "Feedback received successfully");
            out.write(jsonResponse.toString());
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500
        }
    }

}

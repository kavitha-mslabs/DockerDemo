package tneb.ccms.admin;

import java.util.concurrent.ConcurrentHashMap;
import jakarta.servlet.http.HttpSession;

public class SessionRegistry {
	private static final ConcurrentHashMap<String, String> userSessionMap = new ConcurrentHashMap<>();

    public static boolean isSameUserInAnotherSession(String username, String currentSessionId) {
        String existingSessionId = userSessionMap.get(username);
        return existingSessionId != null && !existingSessionId.equals(currentSessionId);
    }

    public static void registerSession(String username, HttpSession session) {
        userSessionMap.put(username, session.getId());
    }

    public static void removeSession(String username) {
        userSessionMap.remove(username);
    }

    public static void clearIfSameSession(String username, String sessionId) {
        if (sessionId.equals(userSessionMap.get(username))) {
            userSessionMap.remove(username);
        }
    }
}

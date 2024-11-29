package org.example;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager {
    private static final Map<String, ClientHandler> clientHandlers = new ConcurrentHashMap<>();

    public static Map<String, ClientHandler> getClientHandlers() {
        return clientHandlers;
    }

    public static List<String> getConnectedUsers() {
        return List.copyOf(clientHandlers.keySet());
    }

    public static void addClientHandler(String nickname, ClientHandler clientHandler) {
        clientHandlers.put(nickname, clientHandler);
    }

    public static void removeClientHandler(String nickname) {
        clientHandlers.remove(nickname);
    }
}

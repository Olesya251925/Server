package org.example;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;


public class ChatServer {
    private static final int PORT = 12345;
    private static final Map<String, ClientHandler> clientHandlers = new ConcurrentHashMap<>();
    private static final List<String> preConnectedClients = Arrays.asList("Анна", "Алиса", "Алина");
    private static final Logger logger = LoggerFactory.getLogger(ChatServer.class);

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Сервер запущен на порту " + PORT);

        // Create pre-connected clients and display them
        System.out.println("Подключенные пользователи:");
        for (String clientName : preConnectedClients) {
            // Проверяем, не был ли уже этот клиент добавлен
            if (!clientHandlers.containsKey(clientName)) {
                ClientHandler preConnectedClient = new ClientHandler(null, clientName);
                clientHandlers.put(clientName, preConnectedClient);
                System.out.println("- " + clientName);
            }
        }


        while (true) {
            Socket clientSocket = serverSocket.accept();
            ClientHandler clientHandler = new ClientHandler(clientSocket);
            new Thread(clientHandler).start();
        }
    }

    public static Map<String, ClientHandler> getClientHandlers() {
        return clientHandlers;
    }

    public static List<String> getConnectedUsers() {
        return new ArrayList<>(clientHandlers.keySet());
    }
    public static void broadcastUserList() {
        String usersList = String.join(",", clientHandlers.keySet());
        for (ClientHandler handler : clientHandlers.values()) {
            handler.sendMessage("USERS:" + usersList);
        }
        logger.info("Обновленный список пользователей отправлен всем клиентам.");
    }

    public static void broadcastNewUser(String nickname) {
        System.out.println("Новый пользователь подключен: " + nickname);
        broadcastMessage("SYSTEM", "Новый пользователь подключен: " + nickname);
    }

    public static void broadcastMessage(String sender, String message) {
        for (ClientHandler handler : clientHandlers.values()) {
            if (!handler.getNickname().equals(sender)) {
                handler.sendMessage("BROADCAST:" + sender + ":" + message);
            }
        }
        logger.info("Отправлено сообщение всем пользователям от " + sender + ": " + message);
    }
}
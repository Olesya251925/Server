package org.example;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {
    private static final Map<String, ClientHandler> clientHandlers = new ConcurrentHashMap<>();
    private static final List<String> preConnectedClients = Arrays.asList("Анна", "Алиса", "Алина");
    private static final Logger logger = LoggerFactory.getLogger(ChatServer.class);
    private static int PORT;

    static {
        // Загрузка конфигурации из файла properties
        Properties properties = new Properties();
        try (InputStream inputStream = ChatServer.class.getClassLoader().getResourceAsStream("server.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
                // Получаем порт из конфигурации
                String port = properties.getProperty("server.port");
                if (port != null) {
                    PORT = Integer.parseInt(port);
                } else {
                    throw new RuntimeException("Порт не задан в файле конфигурации");
                }
            } else {
                throw new FileNotFoundException("Файл server.properties не найден в resources");
            }
        } catch (IOException e) {
            logger.error("Ошибка при загрузке конфигурации", e);
            System.exit(1);
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Сервер запущен на порту " + PORT);

        // Создание пользователей, уже подключенных
        System.out.println("Подключенные пользователи:");
        for (String clientName : preConnectedClients) {
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

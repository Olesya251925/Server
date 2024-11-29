package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatServer {
    private static final List<String> preConnectedClients = Arrays.asList("Анна", "Алиса", "Алина");
    private static final Logger logger = LoggerFactory.getLogger(ChatServer.class);
    private static int PORT;

    static {
        Properties properties = new Properties();
        try (InputStream inputStream = ChatServer.class.getClassLoader().getResourceAsStream("server.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
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

        initializePreConnectedClients();

        while (true) {
            Socket clientSocket = serverSocket.accept();
            ClientHandler clientHandler = new ClientHandler(clientSocket);
            new Thread(clientHandler).start();
        }
    }

    private static void initializePreConnectedClients() {
        System.out.println("Подключенные пользователи:");
        for (String clientName : preConnectedClients) {
            if (!UserManager.getClientHandlers().containsKey(clientName)) {
                ClientHandler preConnectedClient = new ClientHandler(null, clientName);
                UserManager.addClientHandler(clientName, preConnectedClient);
                System.out.println("- " + clientName);
            }
        }
    }

    public static List<String> getConnectedUsers() {
        return new ArrayList<>(UserManager.getClientHandlers().keySet());
    }

    public static Map<String, ClientHandler> getClientHandlers() {
        return UserManager.getClientHandlers();
    }

    public static void broadcastNewUser(String nickname) {
        System.out.println("Новый пользователь подключен: " + nickname);
        broadcastMessage("SYSTEM", "Новый пользователь подключен: " + nickname);
    }

    public static void broadcastMessage(String sender, String message) {
        for (ClientHandler handler : UserManager.getClientHandlers().values()) {
            if (!handler.getNickname().equals(sender)) {
                handler.sendMessage("BROADCAST:" + sender + ":" + message);
            }
        }
        logger.info("Отправлено сообщение всем пользователям от " + sender + ": " + message);
    }
}

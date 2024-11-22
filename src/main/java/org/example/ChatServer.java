package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {

    private static final Logger logger = LoggerFactory.getLogger(ChatServer.class);
    private static final Set<ClientHandler> clientHandlers = new HashSet<>();
    private static final int PORT = 12345;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("Сервер запущен на порту " + PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket);
                clientHandlers.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            logger.error("Ошибка при запуске сервера", e);
        }
    }
}

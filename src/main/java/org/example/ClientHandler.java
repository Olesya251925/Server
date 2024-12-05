package org.example;

import java.io.*;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String nickname;
    private boolean isPreConnected;
    private final MessageHandler messageHandler;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.isPreConnected = false;
        this.messageHandler = new MessageHandler(this);
    }

    public ClientHandler(Socket socket, String nickname) {
        this.socket = socket;
        this.nickname = nickname;
        this.isPreConnected = true;
        this.messageHandler = new MessageHandler(this);
    }

    @Override
    public void run() {
        try {
            if (!isPreConnected) {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                nickname = in.readLine();
                UserManager.addClientHandler(nickname, this);
                ChatServer.broadcastNewUser(nickname);
                logger.info("Новый пользователь подключен: " + nickname);

                sendUserList();

                String message;
                // Читаем сообщения от клиента
                while ((message = in.readLine()) != null) {
                    String[] parts = message.split(":", 2); // Разделяем сообщение на тип и содержимое
                    String messageType = parts[0];  // Тип сообщения
                    String messageContent = parts.length > 1 ? parts[1] : ""; // Содержимое сообщения

                    messageHandler.handleMessage(messageType, messageContent);
                }
            }
        } catch (IOException e) {
            logger.error("Ошибка при обработке сообщений: ", e);
        } finally {
            cleanup();
        }
    }

    // Метод для очистки ресурсов
    private void cleanup() {
        if (!isPreConnected) {
            try {
                socket.close();
                logger.info("Соединение с пользователем " + nickname + " закрыто.");
            } catch (IOException e) {
                logger.error("Ошибка при закрытии соединения: ", e);
            }
            UserManager.removeClientHandler(nickname);
        }
    }

    // Метод для отправки сообщения клиенту
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        } else if (isPreConnected) {
            System.out.println("Сообщение для " + nickname + ": " + message);
            logger.info("Сообщение для предварительно подключенного пользователя " + nickname + ": " + message);
        }
    }

    // Метод для получения никнейма пользователя
    public String getNickname() {
        return nickname;
    }

    // Метод для отправки списка пользователей
    private void sendUserList() {
        messageHandler.sendUserListToClient(); // Отправляем список подключенных пользователей через обработчик сообщений
    }
}
package org.example;

import java.io.*;
import java.net.Socket;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String nickname;
    private boolean isPreConnected;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.isPreConnected = false;
    }

    public ClientHandler(Socket socket, String nickname) {
        this.socket = socket;
        this.nickname = nickname;
        this.isPreConnected = true;
    }

    @Override
    public void run() {
        try {
            if (!isPreConnected) {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                nickname = in.readLine();
                ChatServer.getClientHandlers().put(nickname, this);
                ChatServer.broadcastNewUser(nickname);
                logger.info("Новый пользователь подключен: " + nickname);

                // Send the current user list to the new user
                sendUserList();

                String message;
                while ((message = in.readLine()) != null) {
                    String[] parts = message.split(":", 2);
                    String messageType = parts[0];
                    String messageContent = parts.length > 1 ? parts[1] : "";

                    switch (messageType) {
                        case "GET_USERS":
                            sendUserList();
                            break;
                        case "PRIVATE":
                            handlePrivateMessage(messageContent);
                            break;
                        case "BROADCAST":
                            ChatServer.broadcastMessage(nickname, messageContent);
                            break;
                        default:
                            logger.warn("Неизвестный тип сообщения: " + messageType);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Ошибка при обработке сообщений: ", e);
        } finally {
            if (!isPreConnected) {
                try {
                    socket.close();
                    logger.info("Соединение с пользователем " + nickname + " закрыто.");
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.error("Ошибка при закрытии соединения: ", e);
                }
                ChatServer.getClientHandlers().remove(nickname);
            }
        }
    }

    private void sendUserList() {
        List<String> users = ChatServer.getConnectedUsers();
        sendMessage("USERS:" + String.join(",", users));
        logger.info("Отправлен список пользователей: " + users);
    }

    private void handlePrivateMessage(String messageContent) {
        String[] parts = messageContent.split(":", 2);
        if (parts.length == 2) {
            String recipient = parts[0];
            String content = parts[1];
            sendPrivateMessage(recipient, content);
        }
    }

    private void sendPrivateMessage(String recipientName, String message) {
        ClientHandler recipient = ChatServer.getClientHandlers().get(recipientName);
        if (recipient != null) {
            recipient.sendMessage("PRIVATE:" + nickname + ":" + message);
            logger.info("Отправлено личное сообщение пользователю " + recipientName + " от " + nickname + ": " + message);
        } else {
            sendMessage("SYSTEM:Пользователь " + recipientName + " не найден.");
            logger.warn("Пользователь " + recipientName + " не найден.");
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        } else if (isPreConnected) {
            // For pre-connected users, store messages to be sent when they actually connect
            System.out.println("Сообщение для " + nickname + ": " + message);
            logger.info("Сообщение для предварительно подключенного пользователя " + nickname + ": " + message);
        }
    }

    public String getNickname() {
        return nickname;
    }
}

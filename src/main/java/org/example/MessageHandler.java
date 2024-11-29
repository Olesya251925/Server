package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class MessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);
    private final ClientHandler clientHandler;

    public MessageHandler(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }

    public void handleMessage(String messageType, String messageContent) {
        switch (messageType) {
            case "GET_USERS":
                sendUserListToClient(); // Call the updated public method
                break;
            case "PRIVATE":
                handlePrivateMessage(messageContent);
                break;
            case "BROADCAST":
                handleBroadcastMessage(messageContent);
                break;
            default:
                logger.warn("Неизвестный тип сообщения: " + messageType);
        }
    }

    public void sendUserListToClient() { // Updated to public
        List<String> users = ChatServer.getConnectedUsers();
        clientHandler.sendMessage("USERS:" + String.join(",", users));
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

    private void handleBroadcastMessage(String messageContent) {
        ChatServer.broadcastMessage(clientHandler.getNickname(), messageContent);
        logger.info("Отправлено сообщение всем пользователям от " + clientHandler.getNickname() + ": " + messageContent);
    }

    private void sendPrivateMessage(String recipientName, String message) {
        ClientHandler recipient = ChatServer.getClientHandlers().get(recipientName);
        if (recipient != null) {
            recipient.sendMessage("PRIVATE:" + clientHandler.getNickname() + ":" + message);
            logger.info("Отправлено личное сообщение пользователю " + recipientName + " от " + clientHandler.getNickname() + ": " + message);
        } else {
            logger.warn("Пользователь " + recipientName + " не найден для личного сообщения.");
        }
    }
}

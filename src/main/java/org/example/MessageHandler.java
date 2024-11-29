package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
                sendUserListToClient();
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

    public void sendUserListToClient() {
        List<String> users = ChatServer.getConnectedUsers();
        clientHandler.sendMessage("USERS:" + String.join(",", users));
        logger.info("Отправлен список пользователей: " + users);
    }

    private void handlePrivateMessage(String messageContent) {
        String[] parts = messageContent.split(":", 2);
        if (parts.length == 2) {
            String recipient = parts[0]; // Имя получателя
            String content = parts[1]; // Содержимое сообщения
            sendPrivateMessage(recipient, content);
        } else {
            logger.warn("Некорректный формат личного сообщения от " + clientHandler.getNickname());
            clientHandler.sendMessage("SYSTEM: Некорректный формат личного сообщения. Используйте: PRIVATE:<получатель>:<сообщение>");
        }
    }

    private void handleBroadcastMessage(String messageContent) {
        ChatServer.broadcastMessage(clientHandler.getNickname(), messageContent);
        logger.info("Отправлено сообщение всем пользователям от " + clientHandler.getNickname() + ": " + messageContent);
    }

    private void sendPrivateMessage(String recipientName, String message) {
        ClientHandler recipient = ChatServer.getClientHandlers().get(recipientName);
        if (recipient != null) {
            recipient.sendMessage("PRIVATE:" + clientHandler.getNickname() + ": " + message);
            logger.info("Отправлено личное сообщение пользователю " + recipientName + " от " + clientHandler.getNickname() + ": " + message);
        } else {
            logger.warn("Пользователь " + recipientName + " не найден для личного сообщения.");
            clientHandler.sendMessage("SYSTEM: Пользователь " + recipientName + " не найден.");
        }
    }
}
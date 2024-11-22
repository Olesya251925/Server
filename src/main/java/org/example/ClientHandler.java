package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClientHandler implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String nickname;
    private static final Set<ClientHandler> clientHandlers = new HashSet<>();
    private static final Map<String, ClientHandler> nicknameMap = new HashMap<>();

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Получаем никнейм от клиента
            out.println("Введите ваш никнейм:");
            nickname = in.readLine();
            logger.info("Пользователь подключился: " + nickname);

            // Добавляем клиента в список
            synchronized (clientHandlers) {
                clientHandlers.add(this);
                nicknameMap.put(nickname, this);
            }

            // Обрабатываем сообщения от клиента
            String message;
            while ((message = in.readLine()) != null) {
                logger.info("Получено от " + nickname + ": " + message);
                if (message.startsWith("/private")) {
                    // Личное сообщение
                    sendPrivateMessage(message);
                } else if (message.startsWith("/list")) {
                    // Список пользователей
                    listClients();
                } else {
                    // Широковещательное сообщение
                    broadcastMessage(message);
                }
            }
        } catch (IOException e) {
            logger.error("Ошибка с клиентом " + nickname, e);
        } finally {
            try {
                synchronized (clientHandlers) {
                    clientHandlers.remove(this);
                    nicknameMap.remove(nickname);
                }
                socket.close();
            } catch (IOException e) {
                logger.error("Ошибка при закрытии сокета для " + nickname, e);
            }
        }
    }

    private void sendPrivateMessage(String message) {
        // Ожидаем, что сообщение будет в формате: /private <ник> <сообщение>
        String[] messageParts = message.split(" ", 3);
        if (messageParts.length < 3) {
            out.println("Неверный формат личного сообщения. Используйте: /private <ник> <сообщение>");
            return;
        }
        String targetNickname = messageParts[1];
        String privateMessage = messageParts[2];

        ClientHandler targetClient = nicknameMap.get(targetNickname);
        if (targetClient != null) {
            targetClient.out.println("Личное сообщение от " + nickname + ": " + privateMessage);
            logger.info("Личное сообщение от " + nickname + " к " + targetNickname + ": " + privateMessage);
        } else {
            out.println("Пользователь с никнеймом " + targetNickname + " не найден.");
        }
    }

    private void listClients() {
        out.println("Подключенные пользователи: " + nicknameMap.keySet());
    }

    private void broadcastMessage(String message) {
        synchronized (clientHandlers) {
            for (ClientHandler clientHandler : clientHandlers) {
                if (clientHandler != this) {
                    clientHandler.out.println("Широковещательное сообщение от " + nickname + ": " + message);
                }
            }
        }
        logger.info("Широковещательное сообщение от " + nickname + ": " + message);
    }
}

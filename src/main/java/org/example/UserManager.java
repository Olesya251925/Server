package org.example;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager {
    // Создание потокобезопасной карты, которая будет хранить соответствие между псевдонимами пользователей и их обработчиками
    private static final Map<String, ClientHandler> clientHandlers = new ConcurrentHashMap<>();

    // Метод для получения карты обработчиков клиентов
    public static Map<String, ClientHandler> getClientHandlers() {
        return clientHandlers;
    }

    // Метод для добавления нового обработчика клиента
    public static void addClientHandler(String nickname, ClientHandler clientHandler) {
        clientHandlers.put(nickname, clientHandler);
    }

    // Метод для удаления обработчика клиента по псевдониму
    public static void removeClientHandler(String nickname) {
        clientHandlers.remove(nickname);
    }
}
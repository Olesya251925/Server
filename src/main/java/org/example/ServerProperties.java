package org.example;

import java.io.*;
import java.util.*;

public class ServerProperties {
    private static final Properties properties = new Properties();

    static {
        try (InputStream inputStream = ChatServer.class.getClassLoader().getResourceAsStream("server.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}

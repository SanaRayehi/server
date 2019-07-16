package com.example.server;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MutualData {
    private static HashMap<String , Socket> onlineUsers = new HashMap<>();
     public static void addUser(String username, Socket socket){
        onlineUsers.put(username, socket);
    }
    public static Socket getSocket(String username) {
        return onlineUsers.get(username);
    }
    public static void removeUser(String username) {
        onlineUsers. remove(username);
    }
}

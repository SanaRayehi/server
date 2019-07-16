package com.example.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class ServerChannel extends Thread {
    ChatPeerRepo chatPeerRepo;

    public ServerChannel(ChatPeerRepo chatPeerRepo) {
        this.chatPeerRepo = chatPeerRepo;
    }

    @Override
    public void run() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(12345, 5);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                byte[] data = new byte[8192];
                int size = socket.getInputStream().read(data);
                String info = new String(data, 0,size);
                System.out.println(info);
                System.out.println(size);
                String[] tokens = info.split("[,]");
                String username = tokens[0];
                int chatID = Integer.parseInt(tokens[1].trim());
                if(tokens.length==2) {
                    MutualData.addUser(tokens[0], socket);
                    String peer = chatPeerRepo.searchPeer(chatID, username);
                    ServiceThread serviceThread = new ServiceThread(socket.getInputStream(), chatID, username, peer);
                    serviceThread.start();
                }else {
                    FileDownloadThread downloadThread = new FileDownloadThread(socket, chatID, tokens[2].trim());
                    downloadThread.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

package com.example.server;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class FileDownloadThread extends Thread {
    MessageRepo messageRepo = new MessageRepo();
    private Socket socket;
    private int chatId;
    private String name;

    public FileDownloadThread(Socket socket, int chatId, String name) {
        this.socket = socket;
        this.chatId = chatId;
        this.name = name;
    }

    @Override
    public void run() {
        byte[] data = messageRepo.searchFile(chatId, name);
        ByteInputStream byteInputStream = new ByteInputStream(data, data.length);
        long fileSize = data.length;
        byte[] buffer = new byte[20];
        int size = 0;
        try {
            socket.getOutputStream().write(("size:"+fileSize+":").getBytes());
            System.out.println(fileSize);
            while ((size= byteInputStream.read(buffer))!=-1) {
                socket.getOutputStream().write(buffer,0,size);
            }
            socket.close();
        } catch (IOException e) {
            System.out.println("file sent");
        }
    }
}

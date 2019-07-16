package com.example.server;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Date;

public class ServiceThread  extends Thread{
    private InputStream inputStream;
    private String username;
    private String peer;
    private int chatId;
    private  MessageRepo messageRepo;

    public ServiceThread(InputStream inputStream, int chatId, String username, String peer) {
        this.inputStream = inputStream;
        this.username = username;
        this.chatId = chatId;
        this.peer  = peer;
        this.messageRepo = new MessageRepo();
    }

    @Override
    public void run() {
        int size = 0;
        File temp= null;
        FileOutputStream outputStream = null;
        String fileName="UNKNOWN";
        while (true) {
            byte[] buffer = new byte[8192];
            try {
                size = inputStream.read(buffer);
                if (size==-1) {
                    MutualData.removeUser(username);
                    return;
                }
                String type = new String(buffer,0, 4);
                if(type.equals("text")) {
                    String message = new String(buffer, 5 , size-5);
                    Message text = new Message(chatId,username,new Date(),message);
                    messageRepo.add(text);
                    Socket peerSocket = MutualData.getSocket(peer);
                    if(peerSocket!= null) {
                       peerSocket.getOutputStream().write(("m"+message).getBytes());
                    }
                } else if(type.equals("stat")){
                    String message = new String(buffer, 5 , size-5);
                    Socket peerSocket = MutualData.getSocket(peer);
                    if(peerSocket!= null) {
                        peerSocket.getOutputStream().write(("s"+message).getBytes());
                    }
                } else {
                    if (type.equals("file")) {
                        temp = File.createTempFile("chat",".tmp");
                        outputStream = new FileOutputStream(temp);
                        fileName = new String(buffer, 5 , size-5);;
                    }
                    else if(new String(buffer).trim().endsWith("end")){
                        outputStream.write(buffer, 0, size-3);
                        outputStream.close();
                        byte[] fileData = Files.readAllBytes(temp.toPath());
                        Message fileMessage = new Message(chatId, username, new Date(),fileData,fileName);
                        messageRepo.add(fileMessage);
                        temp.delete();
                        Socket peerSocket = MutualData.getSocket(peer);
                        if(peerSocket!= null) {
                            peerSocket.getOutputStream().write(("f"+fileName).getBytes());
                        }
                    }else {
                        try {
                            outputStream.write(buffer, 0, size);
                        } catch (IOException e) {
                            System.out.println("can not write to file anymore");
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("client disconnect");
                MutualData.removeUser(username);
                return;
            }
        }
    }
}

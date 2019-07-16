package com.example.server;

import com.mysql.cj.jdbc.Blob;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class MessageRepo implements Repo<Message, Integer> {
    @Override
    public void add(Message message) {
        if(message.getMessage() != null) {
            PreparedStatement preparedStatement = DBUtil.createPreparedStatement("INSERT INTO messages(chatid,username,date,message) values(?,?,?,?)");
            try {
                preparedStatement.setInt(1, message.getChatid());
                preparedStatement.setString(2, message.getUsername());
                preparedStatement.setTimestamp(3 , new Timestamp(message.getDate().getTime()));
                preparedStatement.setString(4,message.getMessage());
                preparedStatement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            PreparedStatement preparedStatement = DBUtil.createPreparedStatement("INSERT INTO messages(chatid,username,date,file, filename) values(?,?,?,?,?)");
            try {
                preparedStatement.setInt(1, message.getChatid());
                preparedStatement.setString(2, message.getUsername());
                preparedStatement.setTimestamp(3 , new Timestamp(message.getDate().getTime()));
                preparedStatement.setBlob(4,new Blob(message.getFile() , null));
                preparedStatement.setString(5, message.getFileNAme());
                preparedStatement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }


    public List<Message> getAll(int chatId){
        List<Message> messages = new ArrayList<>();
        PreparedStatement preparedStatement = DBUtil.createPreparedStatement("SELECT * from messages where chatid=? order by date");
        try {
            preparedStatement.setInt(1, chatId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Message message;
                if(resultSet.getString("filename")!=null) {
                    message = new Message(resultSet.getInt("chatid"), resultSet.getString("username"), new Date(resultSet.getTimestamp("date").getTime()),resultSet.getBytes("file"),resultSet.getString("filename"));
                }else{
                    message = new Message(resultSet.getInt("chatid"), resultSet.getString("username"), new Date(resultSet.getTimestamp("date").getTime()),resultSet.getString("message"));
                }
                messages.add(message);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }
    @Override
    public void delete(Integer integer) {

    }

    @Override
    public Message search(Integer integer) {
        return null;
    }

    public byte[] searchFile(int chatID, String fileName) {
        PreparedStatement preparedStatement = DBUtil.createPreparedStatement("SELECT file from messages where chatid=? and filename=?");
        try {
            preparedStatement.setInt(1, chatID);
            preparedStatement.setString(2, fileName);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return resultSet.getBytes("file");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new byte[]{};
    }

    @Override
    public void update(Message message) {

    }
}

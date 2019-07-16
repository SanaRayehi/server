package com.example.server;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

@Component
public class ChatPeerRepo implements Repo<Chat, Integer> {
    @Override
    public void add(Chat chat) {

    }

    public int createChatPeer(String username1, String username2) {
        PreparedStatement searchPreparedStatement = DBUtil.createPreparedStatement("SELECT chat_id from  chat_peers where username1 in (?,?) and username2 in (?,?)");
        PreparedStatement preparedStatement = DBUtil.createPreparedStatement("INSERT into chat_peers (username1, username2) values (?,?)");
        PreparedStatement preparedStatement1 = DBUtil.createPreparedStatement("SELECT chat_id FROM chat_peers ORDER BY chat_id DESC LIMIT 1");

        try {
            searchPreparedStatement.setString(1, username1);
            searchPreparedStatement.setString(2, username2);
            searchPreparedStatement.setString(3, username1);
            searchPreparedStatement.setString(4,username2);
            ResultSet resultSet = searchPreparedStatement.executeQuery();
            if(resultSet.next()){
                return resultSet.getInt("chat_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        try {
            preparedStatement.setString(1, username1);
            preparedStatement.setString(2, username2);
            preparedStatement.execute();
            ResultSet resultSet = preparedStatement1.executeQuery();
            resultSet.next();
            return resultSet.getInt("chat_id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    @Override
    public void delete(Integer i) {
            throw new UnsupportedOperationException();
    }

    @Override
    public Chat search(Integer i) { return null;
    }

    public String searchPeer(Integer i, String username) {
        PreparedStatement preparedStatement = DBUtil.createPreparedStatement("SELECT * FROM chat_peers where chat_id=?");
        try {
            preparedStatement.setInt(1, i);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            String username1 = resultSet.getString("username1");
            String username2 = resultSet.getString("username2");
            if(username1.equals(username)) return username2;
            else return username1;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void update(Chat chat) {

    }

    public List<Chat> findAllChatsForUsername(String username) {
        List<Chat> chats = new ArrayList<>();
        PreparedStatement preparedStatement = DBUtil.createPreparedStatement("SELECT * from chat_peers where username1=? or username2=?");
        try {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String username1 = resultSet.getString("username1");
                Chat chat;
                if (username1.equals(username)) {
                    chat = new Chat(resultSet.getString("username2"), resultSet.getInt("chat_id"));
                }
                else {
                    chat = new Chat(username1, resultSet.getInt("chat_id"));
                }
                chats.add(chat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return chats;
    }
}

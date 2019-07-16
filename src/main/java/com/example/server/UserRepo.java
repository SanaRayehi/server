package com.example.server;

import com.mysql.cj.jdbc.Blob;
import org.springframework.mail.MailSender;
import org.springframework.stereotype.Component;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Random;

@Component
public class UserRepo implements Repo<User, String> {
    Random random = new Random();
    @Override
    public void add(User user) {
        PreparedStatement preparedStatement = DBUtil.createPreparedStatement("INSERT INTO users values (?,?,?,?,?,?,0,0,?)");
        try {
            preparedStatement.setString(1, user.getName());
            preparedStatement.setString(2, user.getLastname());
            preparedStatement.setString(3, user.getEmail());
            preparedStatement.setString(4, user.getUsername());
            preparedStatement.setString(5, user.getPassword());
            preparedStatement.setBlob(6, new Blob(user.getPicture(), null));
            preparedStatement.setTimestamp(7, new Timestamp(new Date().getTime()));
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int activeCodeForUser(User user) throws ActivationCodeGenerationFailedException {
        PreparedStatement preparedStatement2 = DBUtil.createPreparedStatement("INSERT INTO activecode values (?,?)");
        try {
            int activecode = random.nextInt(89999)+10000;
            preparedStatement2.setString(1, user.getUsername());
            preparedStatement2.setInt(2, activecode);
            preparedStatement2.execute();
            return activecode;
        } catch (SQLException e) {
            throw new ActivationCodeGenerationFailedException();
        }
    }

    public boolean activeUser(String username, int code) {
        PreparedStatement preparedStatement = DBUtil.createPreparedStatement("SELECT * FROM activecode where username=? and activecode=?");
        try {
            preparedStatement.setString(1, username);
            preparedStatement.setInt(2, code);
            ResultSet resultset = preparedStatement.executeQuery();
            if(resultset.next()) {
                PreparedStatement preparedStatement1 = DBUtil.createPreparedStatement("UPDATE users set active=1 where username=?");
                preparedStatement1.setString(1, username);
                preparedStatement1.execute();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    @Override
    public void delete(String s) {
        PreparedStatement preparedStatement = DBUtil.createPreparedStatement("delete from users where username = ?");
        try {
            preparedStatement.setString(1, s);
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public User search(String s) {
        PreparedStatement preparedStatement = DBUtil.createPreparedStatement("SELECT * from users where username=?");
        try {
            preparedStatement.setString(1, s);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) return null;

            User user = new User(resultSet.getString("name"), resultSet.getString("lastname"),
                    resultSet.getString("email"), resultSet.getString("username"), resultSet.getString("password"), resultSet.getBytes("picture"), resultSet.getBoolean("active"));
            return user;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void update(User user) {
        PreparedStatement preparedStatement = DBUtil.createPreparedStatement("UPDATE users set name=?, lastname=?, picture=?");
        try {
            preparedStatement.setString(1, user.getName());
            preparedStatement.setString(2, user.getLastname());
            preparedStatement.setBlob(3, new Blob(user.getPicture() , null));
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateLoginStatus(String username, boolean status) {
        if(status== true) {
            PreparedStatement preparedStatement = DBUtil.createPreparedStatement("UPDATE users set login_status=? where username=?");
            try {
                preparedStatement.setBoolean(1, status);
                preparedStatement.setString(2, username);
                preparedStatement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }else {
            PreparedStatement preparedStatement = DBUtil.createPreparedStatement("UPDATE users set login_status=? , login_date=? where username=?");
            try {
                preparedStatement.setBoolean(1, status);
                preparedStatement.setTimestamp(2, new Timestamp(new Date().getTime()));
                preparedStatement.setString(3, username);
                preparedStatement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public UserStatusDto getUserStatus(String username) {
        PreparedStatement preparedStatement = DBUtil.createPreparedStatement("SELECT login_status, login_date from users where username=?");
        try {
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            boolean status = resultSet.getBoolean("login_status");
            UserStatusDto statusDto;
            if(status) {
                statusDto = new UserStatusDto(true);
            }else {
                Timestamp timestamp = resultSet.getTimestamp("login_date");
                statusDto = new UserStatusDto(new Date(timestamp.getTime()),false);
            }
            return statusDto;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}

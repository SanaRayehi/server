 package com.example.server;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ServerServiceHandler implements ServiceHandler {

    @Autowired
    ServerMailSender ServerMailSender;
    @Autowired
    UserRepo userRepo;
    @Autowired
    ChatPeerRepo chatPeerRepo;
    @Autowired
    MessageRepo messageRepo;
    @Override
    public void register(SignUpDto signUpDto) {
        String password = DigestUtils.sha1Hex(signUpDto.getPassword());
        User user = new User(signUpDto);
        user.setPassword(password);
        userRepo.add(user);
        try {
            int activationcode = userRepo.activeCodeForUser(user);
            ServerMailSender.sendEmailActivationCode(user.getUsername(), activationcode, user.getEmail());
        } catch (ActivationCodeGenerationFailedException e) {
            e.printStackTrace();
            System.out.println("can not create active code");
        }

    }

    @Override
    public boolean activeUser(String username, int activecode) {
        return userRepo.activeUser(username, activecode);
    }

    @Override
    public UserBaseInfoDto login(String username, String password) throws UserNotFoundException {
        password = DigestUtils.sha1Hex(password);
        User user = userRepo.search(username);
        if(user== null) throw new UserNotFoundException();
        if (!user.isActive()) throw new UserNotFoundException();
        if(!user.getPassword().equals(password)) throw new UserNotFoundException();
        if(userRepo.getUserStatus(username).isStatus()) throw new UserNotFoundException();
        UserBaseInfoDto userBaseInfoDto = new UserBaseInfoDto(user.getName(), user.getLastname(), user.getPicture(), user.getUsername());
        List<Chat> chatList = chatPeerRepo.findAllChatsForUsername(username);
        for(Chat chat: chatList) {
            User temp = userRepo.search(chat.getUsername());
            chat.setName(temp.getName());
            chat.setLastname(temp.getLastname());
            chat.setPicture(temp.getPicture());
        }
        userBaseInfoDto.setChats(chatList);
        userRepo.updateLoginStatus(username , true);
        return userBaseInfoDto;
    }

     @Override
     public void update(UpdateUserDto updateUserDto) {
         User user = new User(updateUserDto);
         userRepo.update(user);
     }

     @Override
     public List<Message> getHistory(int chatid) {
         return messageRepo.getAll(chatid);
     }

    @Override
    public Chat findUser(String username, String peer) throws UserNotFoundException {
        User user = userRepo.search(peer.substring(1));
        if(user == null){
            throw new UserNotFoundException();
        }
        else {
            int chatid = chatPeerRepo.createChatPeer(username, peer.substring(1));
            Chat chat = new Chat(peer.substring(1), chatid);
            chat.setName(user.getName());
            chat.setLastname(user.getLastname());
            chat.setPicture(user.getPicture());
            return chat;
        }
    }

    @Override
    public void logout(String username) {
        userRepo.updateLoginStatus(username , false);
    }

    @Override
    public UserStatusDto userStatus(String username) {
        return userRepo.getUserStatus(username);
    }

    @Override
    public void deleteUser(String username) {
        userRepo.delete(username);
    }
}

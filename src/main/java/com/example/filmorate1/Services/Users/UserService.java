package com.example.filmorate1.Services.Users;

import com.example.filmorate1.BacisClasses.User;

import java.util.ArrayList;

public interface UserService {

    public ArrayList<User> getUserFriendsList(int userId);
    public String addFriend(int userId, int friendId);
    public void removeFriend(int userId, int friendId);
    public ArrayList<User> getMutualFriends(int userId1, int userId2);
}

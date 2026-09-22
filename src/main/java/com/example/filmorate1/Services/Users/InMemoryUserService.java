package com.example.filmorate1.Services.Users;

import com.example.filmorate1.BacisClasses.User;
import com.example.filmorate1.Exceptions.FriendsExceptions.NonExistingFriendException;
import com.example.filmorate1.Exceptions.UserExceptions.NonExistingUserException;
import com.example.filmorate1.Storages.Users.InMemoryUserStorage;
import com.example.filmorate1.Storages.Users.UserStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Service
public class InMemoryUserService implements UserService {

    UserStorage userStorage;

    @Autowired
    public InMemoryUserService(InMemoryUserStorage userStorage) {
        this.userStorage = userStorage;
    }

    private boolean checkFriendExists(int userId, int friendId) {
        if(!checkUserExists(userId))
        {
            throw new NonExistingUserException(userId);
        }
        if(!checkUserExists(friendId))
        {
            throw new NonExistingUserException(friendId);
        }

        User existingUser = userStorage.getUserById(userId);
        return existingUser.getFriendsList().contains(friendId);
    }

    private boolean checkUserExists(int userId) {
        User existingUser=userStorage.getUserById(userId);
        return existingUser!=null;
    }

    public ArrayList<User> getUserFriendsList(int userId) throws NonExistingUserException {
        Set<Integer> friendsIds=null;
        ArrayList<User> friendsList = new ArrayList<>();

        if(!checkUserExists(userId)) {
            throw new NonExistingUserException(userId);
        }

        friendsIds=userStorage.getUsersList().get(userId).getFriendsList();

        for(Integer friendId : friendsIds) {
            friendsList.add(userStorage.getUserById(friendId));
        }

        return friendsList;
    }

    @Override
    public String addFriend(int userId, int friendId) {
        if (!checkUserExists(userId)) {
            throw new NonExistingUserException(userId);
        }
        if (!checkUserExists(friendId)) {
            throw new NonExistingUserException(friendId);
        }

        userStorage.getUserById(userId).getFriendsList().add(friendId);
        userStorage.getUserById(friendId).getFriendsList().add(userId);
        return "Added friend";
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        if (!checkUserExists(userId)) {
            throw new NonExistingUserException(userId);
        }
        if (!checkUserExists(friendId)) {
            throw new NonExistingUserException(friendId);
        }
        if(!checkFriendExists(userId, friendId)) {
            throw new NonExistingFriendException(userId, friendId);
        }

        userStorage.getUserById(userId).getFriendsList().remove(friendId);
        userStorage.getUserById(friendId).getFriendsList().remove(userId);
    }

    @Override
    public ArrayList<User> getMutualFriends(int userId, int friendId) {

        if (!checkUserExists(userId)) {
            throw new NonExistingUserException(userId);
        }
        if (!checkUserExists(friendId)) {
            throw new NonExistingUserException(friendId);
        }
        if (!checkFriendExists(userId, friendId)) {
            throw new NonExistingFriendException(userId, friendId);
        }

        HashSet<Integer> mutualUsersIds = new HashSet<>(userStorage.getUserById(userId).getFriendsList());
        mutualUsersIds.retainAll(userStorage.getUserById(friendId).getFriendsList());

        ArrayList<User> users = new ArrayList<>();
        for (int i = 0; i < userStorage.getUsersList().size(); i++) {
            if (mutualUsersIds.contains(userStorage.getUsersList().get(i).getId())) {
                users.add(userStorage.getUsersList().get(i));
            }
        }
        return users;
    }
}


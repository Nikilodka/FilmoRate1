package com.example.filmorate1.Storages.Users;

import com.example.filmorate1.Exceptions.UserExceptions.NonExistingUserException;
import com.example.filmorate1.Exceptions.UserExceptions.UserAlreadyExistsException;
import com.example.filmorate1.BacisClasses.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public interface UserStorage {
    public Map<Integer,User> getUsersList();
    public void setUsersList(Map<Integer,User> uesersList);
    public User getUserById(int userId) throws NonExistingUserException;
    public void addUser(User user) throws UserAlreadyExistsException;
    public void removeUser(int userId) throws NonExistingUserException;
    public void updateUser(int id,User user) throws NonExistingUserException;
}

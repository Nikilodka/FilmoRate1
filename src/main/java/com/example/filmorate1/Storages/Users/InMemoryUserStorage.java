package com.example.filmorate1.Storages.Users;

import com.example.filmorate1.BacisClasses.Film.Film;
import com.example.filmorate1.Exceptions.UserExceptions.NonExistingUserException;
import com.example.filmorate1.Exceptions.UserExceptions.UserAlreadyExistsException;
import com.example.filmorate1.BacisClasses.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryUserStorage implements UserStorage {
    @Getter
    private Map<Integer,User> usersList;

    @Getter
    @Setter
    private int currentID=0;

    public InMemoryUserStorage() {
        usersList = new HashMap<>();
    }
    public boolean checkUserExists(User user) {
        for(User u: usersList.values())
        {
            if(u.getLogin().equals(user.getLogin()) && u.getEmail().equals(user.getEmail()))
            {
                return true;
            }
        }
        return false;
    }

    private void checkEmptyName(User user)
    {
        if(user.getName().isEmpty())
        {
            user.setName(user.getLogin());
        }
    }

    public void setUsersList(Map<Integer,User> usersList) {
        this.usersList = usersList;
    }

    @Override
    public User getUserById(int userId) throws NonExistingUserException
    {
        User user = usersList.get(userId);
        if(user == null)
        {
            throw new NonExistingUserException(userId);
        }
        return user;
    }


    @Override
    public void addUser(User user) throws UserAlreadyExistsException {
        if(checkUserExists(user))
            throw new UserAlreadyExistsException(user.getLogin(), user.getEmail());

        checkEmptyName(user);
        user.setId(currentID++);
        usersList.put(user.getId(), user);
        currentID++;
    }

    @Override
    public void removeUser(int userId) throws NonExistingUserException {
        User user = usersList.get(userId);
        if(user == null)
            throw new NonExistingUserException(userId);
        usersList.remove(userId);
    }

    @Override
    public void updateUser(int filmId, User user) throws NonExistingUserException {
        try{
            usersList.replace(filmId,user);
        }
        catch(Exception e)
        {
            throw new NonExistingUserException(filmId);
        }
    }

}

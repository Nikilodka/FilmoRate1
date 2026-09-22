package com.example.filmorate1.Exceptions.UserExceptions;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(int id) {
        super("User with id " + id + " already exists");
    }
    public UserAlreadyExistsException(String login,String email) {
        super("User with login: " + login + " and email: "+email+" already exists");

    }
}

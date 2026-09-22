package com.example.filmorate1.Exceptions.UserExceptions;

public class NonExistingUserException extends RuntimeException {
    public NonExistingUserException(int id) {
        super("User with id " + id + " does not exist");
    }
    public NonExistingUserException(String login, String email) {
        super("User with login: " + login + " and email: "+email+" does not exist");
    }
}

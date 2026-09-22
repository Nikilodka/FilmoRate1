package com.example.filmorate1.Exceptions.FriendsExceptions;

public class AlreadyFriendsException extends RuntimeException {
    public AlreadyFriendsException(int userId,int friendId) {
        super("Users with id " + userId + " already has friend with id " + friendId);
    }
}

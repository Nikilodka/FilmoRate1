package com.example.filmorate1.Exceptions.FriendsExceptions;

public class SelfFriendshipException extends RuntimeException {
    public SelfFriendshipException(int id) {
        super("Cannot add self friendship to " + id);
    }
}

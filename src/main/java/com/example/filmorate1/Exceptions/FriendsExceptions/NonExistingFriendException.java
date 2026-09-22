package com.example.filmorate1.Exceptions.FriendsExceptions;

public class NonExistingFriendException extends RuntimeException {
    public NonExistingFriendException(int userId1,int userId2) {
        super("Users with ID= " + userId1 + " and ID= " + userId2 + " are not friends");
    }
}

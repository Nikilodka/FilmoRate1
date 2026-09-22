package com.example.filmorate1.Exceptions.FriendsExceptions;

public class NonExistingFriendshipRequestException extends RuntimeException {
    public NonExistingFriendshipRequestException(int userId,int friendId) {
        super("User " + userId + " doesn't have friendship request to " + friendId);
    }
}

package com.example.filmorate1.Exceptions.FriendsExceptions;

public class FriendshipRequestAlreadyExists extends RuntimeException {
    public FriendshipRequestAlreadyExists(int senderId, int receiverId) {
        super("User with id " + senderId + " already sent a friendship request to user with id " + receiverId);
    }
}

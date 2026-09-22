package com.example.filmorate1.Exceptions.LikeExceptions;

public class LikeAlreadyExistsException extends RuntimeException {
    public LikeAlreadyExistsException(int userId,int filmId) {
        super("User with id " + userId + " already liked film with id " + filmId);
    }
}

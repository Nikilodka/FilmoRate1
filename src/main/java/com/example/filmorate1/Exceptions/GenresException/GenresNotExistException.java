package com.example.filmorate1.Exceptions.GenresException;

public class GenresNotExistException extends RuntimeException {
    public GenresNotExistException(String genresIds) {

        super("Genres with ids: " + genresIds + " not exist");
    }
}

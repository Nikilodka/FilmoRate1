package com.example.filmorate1.Exceptions.FilmExceptions;

import java.time.LocalDate;

public class FilmAlreadyExistsException extends Exception {
    public FilmAlreadyExistsException(String title, LocalDate releaseDate) {
        super("Film with title " + title + "and release date "+ releaseDate+" already exists");
    }
    public FilmAlreadyExistsException(int filmId) {
        super("Film with id " + filmId + " already exists");
    }
}

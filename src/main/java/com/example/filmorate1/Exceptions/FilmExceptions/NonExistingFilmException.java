package com.example.filmorate1.Exceptions.FilmExceptions;

import java.time.LocalDate;

public class NonExistingFilmException extends RuntimeException {
    public NonExistingFilmException(int filmId) {
      super("Film with id "+filmId+ " does not exist");
    }
    public NonExistingFilmException(String title, LocalDate releaseDate) {
      super("Film with title " + title + " and release date" + releaseDate +"was not found");
    }
}

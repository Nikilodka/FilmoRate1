package com.example.filmorate1.Storages.Films;

import com.example.filmorate1.BacisClasses.Film.FilmRequest;
import com.example.filmorate1.Exceptions.FilmExceptions.FilmAlreadyExistsException;
import com.example.filmorate1.BacisClasses.Film.Film;
import com.example.filmorate1.Exceptions.FilmExceptions.NonExistingFilmException;

import java.util.Map;

public interface FilmStorage {
    public Map<Integer,Film> getFilmsList();
    public Film getFilmById(int id);
    public void addFilm(Film film) throws FilmAlreadyExistsException;
    public void updateFilm(int filmId, Film film) throws NonExistingFilmException;
    public void removeFilm(int filmId) throws NonExistingFilmException;
    public void setFilmsList(Map<Integer,Film> filmsList);
}

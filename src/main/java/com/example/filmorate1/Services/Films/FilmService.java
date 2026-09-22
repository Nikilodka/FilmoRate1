package com.example.filmorate1.Services.Films;

import com.example.filmorate1.BacisClasses.Film.Film;

import java.util.List;

public interface FilmService {

    public void addLike(int userId, int filmId);
    public void removeLike(int userId, int filmId);
    public List<Film> getMostLikedFilms(int maxFilmsCount);
    public List<Film> getMostLikedFilms();
    public boolean checkFilmExists(int filmId);
    public boolean checkUserExists(int userId);
    public boolean checkLikeExists(int filmId, int userId);
}

package com.example.filmorate1.Services.Films;

import com.example.filmorate1.BacisClasses.Film.Film;
import com.example.filmorate1.BacisClasses.User;
import com.example.filmorate1.Exceptions.FilmExceptions.NonExistingFilmException;
import com.example.filmorate1.Exceptions.LikeExceptions.NonExistingLikeException;
import com.example.filmorate1.Exceptions.UserExceptions.NonExistingUserException;
import com.example.filmorate1.Storages.Films.FilmStorage;
import com.example.filmorate1.Storages.Films.InMemoryFilmStorage;
import com.example.filmorate1.Storages.Users.InMemoryUserStorage;
import com.example.filmorate1.Storages.Users.UserStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class InMemoryFilmService implements FilmService {

    private FilmStorage filmStorage;
    private UserStorage userStorage;

    @Autowired
    public InMemoryFilmService(InMemoryFilmStorage filmStorage, InMemoryUserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    @Override
    public boolean checkUserExists(int userId) {
        User existingUser=userStorage.getUserById(userId);
        return existingUser != null;
    }

    @Override
    public boolean checkFilmExists(int filmId) {
        Film existingFilm=filmStorage.getFilmById(filmId);
        return existingFilm != null;
    }

    @Override
    public boolean checkLikeExists(int userId, int filmId) {

        checkParams(userId, filmId);
        User existingUser=userStorage.getUserById(userId);

        return existingUser.getLikedFilmsList().contains(filmId);

    }

    public void checkParams(int userId, int filmId) {
        if (!checkUserExists(userId)) {
            throw new NonExistingUserException(userId);
        } else if (!checkFilmExists(filmId)) {
            throw new NonExistingFilmException(filmId);
        }
    }

    @Override
    public void addLike(int userId, int filmId) {
        checkParams(userId, filmId);

        Film existingFilm=filmStorage.getFilmById(filmId);
        existingFilm.addLike();
        filmStorage.updateFilm(filmId,existingFilm);

        User existingUser=userStorage.getUserById(userId);
        existingUser.getLikedFilmsList().add(filmId);
        userStorage.updateUser(userId,existingUser);

    }

    @Override
    public void removeLike(int userId, int filmId) {
        checkParams(userId, filmId);

        if (checkLikeExists(filmId, userId)) {
            Film existingFilm=filmStorage.getFilmById(filmId);
            existingFilm.removeLike();
            filmStorage.updateFilm(filmId,existingFilm);

            User existingUser=userStorage.getUserById(userId);
            existingUser.getLikedFilmsList().remove(filmId);
            userStorage.updateUser(userId,existingUser);

        } else
            throw new NonExistingLikeException("Like not found");
    }


    @Override
    public List<Film> getMostLikedFilms(int maxFilmsCount) {
        ArrayList<Film> filmsList=new ArrayList<>();

        for(int i=0; i<filmStorage.getFilmsList().size(); i++) {
            filmsList.add(filmStorage.getFilmsList().get(i));
        }

        filmsList.sort((f1, f2) -> Integer.compare(f2.getLikesCount(), f1.getLikesCount()));

        return filmsList.stream().limit(maxFilmsCount).collect(Collectors.toList());
    }


    @Override
    public List<Film> getMostLikedFilms() {
        int maxFilmsCount=10;
        ArrayList<Film> filmsList=new ArrayList<>();

        for(int i=0; i<filmStorage.getFilmsList().size(); i++) {
            filmsList.add(filmStorage.getFilmsList().get(i));
        }

        filmsList.sort((f1, f2) -> Integer.compare(f2.getLikesCount(), f1.getLikesCount()));

        return filmsList.stream().limit(maxFilmsCount).collect(Collectors.toList());
    }

}






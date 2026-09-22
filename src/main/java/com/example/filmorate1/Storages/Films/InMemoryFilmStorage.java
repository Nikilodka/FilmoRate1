package com.example.filmorate1.Storages.Films;

import com.example.filmorate1.BacisClasses.Film.FilmRequest;
import com.example.filmorate1.Exceptions.FilmExceptions.FilmAlreadyExistsException;
import com.example.filmorate1.BacisClasses.Film.Film;
import com.example.filmorate1.Exceptions.FilmExceptions.NonExistingFilmException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    @Getter

    private Map<Integer,Film> filmsList;

    @Getter
    @Setter
    private int currentID=0;

    public InMemoryFilmStorage() {
        filmsList = new HashMap<>();
    }

    public boolean checkFilmExists(Film film) {
        for(Film f: filmsList.values())
        {
            if(f.getTitle().equals(film.getTitle()) && f.getReleaseDate().equals(film.getReleaseDate()))
            {
                return true;
            }
        }
        return false;
    }
    @Override
    public Film getFilmById(int filmId)
    {
        Film existingFilm = filmsList.get(filmId);

        if(existingFilm == null)
            throw new NonExistingFilmException(filmId);

        return existingFilm;
    }

    public void setFilmsList(Map<Integer,Film> filmsList) {
        this.filmsList = filmsList;
    }

    @Override
    public void addFilm(Film film) throws FilmAlreadyExistsException {
        if(checkFilmExists(film))
            throw new FilmAlreadyExistsException(film.getTitle(), film.getReleaseDate());

        film.setId(currentID);
        filmsList.put(film.getId(), film);
        currentID++;

    }

    @Override
    public void removeFilm(int filmId) throws NonExistingFilmException {
        Film film = filmsList.get(filmId);
        if(film == null)
            throw new NonExistingFilmException(filmId);

        filmsList.remove(filmId);
    }

    @Override
    public void updateFilm(int filmId, Film film) throws NonExistingFilmException {
        try{
            filmsList.replace(filmId, film);
        }
        catch (Exception e) {
            throw new NonExistingFilmException(filmId);
        }

    }

}

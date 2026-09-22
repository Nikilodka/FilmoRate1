package com.example.filmorate1.Services.Films;

import com.example.filmorate1.BacisClasses.Film.Film;
import com.example.filmorate1.BacisClasses.Film.FilmGenre;
import com.example.filmorate1.BacisClasses.Film.FilmRequest;
import com.example.filmorate1.BacisClasses.Film.MPARating;
import com.example.filmorate1.Exceptions.FilmExceptions.NonExistingFilmException;
import com.example.filmorate1.Exceptions.LikeExceptions.LikeAlreadyExistsException;
import com.example.filmorate1.Exceptions.LikeExceptions.NonExistingLikeException;
import com.example.filmorate1.Exceptions.UserExceptions.NonExistingUserException;
import com.example.filmorate1.Storages.Films.Mappers.FilmGenreMapper;
import com.example.filmorate1.Storages.Films.Mappers.FilmRowMapper;
import com.example.filmorate1.Storages.Films.Mappers.MPARatingMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Qualifier("filmDBService")
@Slf4j
public class FilmDBService implements FilmService{

    JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmDBService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean checkFilmExists(int filmId) {
        String sql = "SELECT COUNT(*) FROM films WHERE id=?";

        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{filmId}, Integer.class);
        if (count == 0 || count == null) {
            throw new NonExistingFilmException(filmId);
        }

        return true;
    }

    @Override
    public boolean checkUserExists(int userId) {
        String sql = "SELECT COUNT(*) FROM users WHERE id=?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{userId}, Integer.class);
        if (count == 0 || count == null) {
            throw new NonExistingUserException(userId);
        }
        return true;
    }

    @Override
    public boolean checkLikeExists(int userId, int filmId) {
        log.info("Checking if like " + filmId + " exists");
        String sql="SELECT COUNT(*) FROM film_likes WHERE id_film=? AND id_user=?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, filmId, userId);
        if (count == 0) {
            return false;
        }
        else{
            return true;
        }
    }

    @Override
    @Transactional
    public void addLike(int userId, int filmId) throws LikeAlreadyExistsException {
        if(!checkFilmExists(filmId)) {
            throw new NonExistingFilmException(filmId);
        }
        if(!checkUserExists(userId)) {
            throw new NonExistingUserException(userId);
        }
        if(checkLikeExists(userId, filmId)) {
            throw new LikeAlreadyExistsException(userId,filmId);
        }

        String sql = "INSERT INTO film_likes (id_user,id_film) VALUES (?, ?)";

        String filmSql="UPDATE films SET likes_count=likes_count+1 WHERE id=?";

        try{
            jdbcTemplate.update(sql, userId, filmId);
            jdbcTemplate.update(filmSql, filmId);
            log.info("Film liked successfully");
        }
        catch(Exception e){
            log.info("Film liked failed");
            throw new RuntimeException("Unable to like film: "+e.getMessage());
        }
    }

    @Override
    @Transactional
    public void removeLike(int userId, int filmId) {
        if(!checkFilmExists(filmId)) {
            throw new NonExistingFilmException(filmId);
        }
        if(!checkUserExists(userId)) {
            throw new NonExistingUserException(userId);
        }
        if(!checkLikeExists(userId, filmId)) {
            throw new NonExistingLikeException("Like not exists");
        }


        String sql="DELETE FROM film_likes WHERE id_film = ? AND id_user = ?";

        String filmSql="UPDATE films SET likes_count=likes_count-1 WHERE id=?";


        try{
            jdbcTemplate.update(sql, userId, filmId);
            jdbcTemplate.update(filmSql, filmId);
            log.info("Like removed successfully");
        }
        catch(Exception e){
            log.info("Like removal failed");
            throw new RuntimeException("Unable to remove like from film: "+e.getMessage());
        }
    }

    @Override
    public List<Film> getMostLikedFilms(int limit) {
        String sql = "SELECT films.id, " +
                "films.title, " +
                "films.description, " +
                "films.release_date, " +
                "films.duration, " +
                "films.likes_count, " +
                "mpa_rating.rating FROM films LEFT OUTER JOIN " +
                "mpa_rating on films.mpa_rating_id =mpa_rating.id ORDER BY films.likes_count DESC LIMIT ?";

        try{
            List<Film> filmsList= jdbcTemplate.query(sql,new FilmRowMapper(),limit);

            Map<Integer, List<FilmGenre>> filmsGenres=getFilmsGenres();

            for(Film film : filmsList)
            {
                film.setGenres(new ArrayList<>(filmsGenres.getOrDefault(film.getId(), Collections.emptyList())));
            }

            return filmsList;
        }
        catch(Exception e){
            log.error("Cannot get films list", e);
            throw new RuntimeException("Unable to retrieve films from database",e);
        }
    }

    @Override
    public List<Film> getMostLikedFilms() {
        String sql = "SELECT films.id, " +
                "films.title, " +
                "films.description, " +
                "films.release_date, " +
                "films.duration, " +
                "films.likes_count, " +
                "mpa_rating.rating FROM films LEFT OUTER JOIN " +
                "mpa_rating on films.mpa_rating_id =mpa_rating.id ORDER BY films.likes_count DESC";

        try{
            List<Film> filmsList= jdbcTemplate.query(sql,new FilmRowMapper());

            Map<Integer, List<FilmGenre>> filmsGenres=getFilmsGenres();

            for(Film film : filmsList)
            {
                film.setGenres(new ArrayList<>(filmsGenres.getOrDefault(film.getId(), Collections.emptyList())));
            }

            return filmsList;
        }
        catch(Exception e){
            log.error("Cannot get films list", e);
            throw new RuntimeException("Unable to retrieve films from database",e);
        }
    }

    public Map<Integer, List<FilmGenre>> getFilmsGenres(){//нужно ли
        String sqlForGenres = "SELECT film_genres.film_id, genres.id, genres.genre FROM films " +
                "LEFT OUTER JOIN film_genres on films.id = film_genres.film_id " +
                "LEFT OUTER JOIN genres on film_genres.genre_id = genres.id";

        Map<Integer, List<FilmGenre>> genres = new HashMap<>();

        try {

            jdbcTemplate.query(sqlForGenres, rs -> {
                int filmId = rs.getInt("film_id");
                int genreId = rs.getInt("id");
                String genre = rs.getString("genre");

                genres.computeIfAbsent(filmId, k -> new ArrayList<>()).add(new FilmGenre(genreId, genre));
            });
            return genres;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

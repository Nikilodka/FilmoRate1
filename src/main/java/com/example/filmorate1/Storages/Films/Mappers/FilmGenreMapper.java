package com.example.filmorate1.Storages.Films.Mappers;

import com.example.filmorate1.BacisClasses.Film.Film;
import com.example.filmorate1.BacisClasses.Film.FilmGenre;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FilmGenreMapper implements RowMapper<FilmGenre> {

    public FilmGenre mapRow(ResultSet rs, int rowNum) throws SQLException {
        FilmGenre genre = new FilmGenre(rs.getInt("id"), rs.getString("genre"));
        return genre;
    }
}

package com.example.filmorate1.Storages.Films.Mappers;

import com.example.filmorate1.BacisClasses.Film.Film;
import org.postgresql.util.PGInterval;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;

public class FilmRowMapper implements RowMapper<Film> {

    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getInt("id"));
        film.setTitle(rs.getString("title"));
        film.setDescription(rs.getString("description"));

        PGInterval pgInterval=(PGInterval)rs.getObject("duration");
        if(pgInterval.getYears()==0 && pgInterval.getMonths()==0)
        {
            Duration duration = Duration.ofHours(pgInterval.getHours())
                    .plusMinutes(pgInterval.getMinutes())
                    .plusSeconds((long)pgInterval.getSeconds());
            film.setDuration(duration);
        }

        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setLikesCount(rs.getInt("likes_count"));

        return film;
    }

}

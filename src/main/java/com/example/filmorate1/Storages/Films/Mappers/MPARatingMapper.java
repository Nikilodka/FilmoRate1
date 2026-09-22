package com.example.filmorate1.Storages.Films.Mappers;

import com.example.filmorate1.BacisClasses.Film.MPARating;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;


public class MPARatingMapper implements RowMapper<MPARating> {
    public MPARating mapRow(ResultSet rs, int rowNum) throws SQLException {
        MPARating mparating = new MPARating();
        mparating.setId(rs.getInt("id"));
        mparating.setRating(rs.getString("rating"));
        return mparating;
    }
}

package com.example.filmorate1.Storages.Films.DAO;


import com.example.filmorate1.BacisClasses.Film.Film;
import com.example.filmorate1.BacisClasses.Film.FilmGenre;
import com.example.filmorate1.BacisClasses.Film.FilmRequest;
import com.example.filmorate1.BacisClasses.Film.MPARating;
import com.example.filmorate1.Exceptions.FilmExceptions.FilmAlreadyExistsException;
import com.example.filmorate1.Exceptions.FilmExceptions.NonExistingFilmException;
import com.example.filmorate1.Exceptions.GenresException.GenresNotExistException;
import com.example.filmorate1.Storages.Films.FilmStorage;
import com.example.filmorate1.Storages.Films.Mappers.FilmGenreMapper;
import com.example.filmorate1.Storages.Films.Mappers.FilmRowMapper;
import com.example.filmorate1.Storages.Films.Mappers.MPARatingMapper;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PGInterval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Qualifier("filmDBStorage")
@Slf4j
public class FilmDBStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmDBStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<Integer, List<FilmGenre>> getFilmsGenres(){
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

    @Override
    public void setFilmsList(Map<Integer, Film> filmsList)
    {

    }


    public void insertFilmsGenresIds(Film film){
        String sql="INSERT INTO film_genres VALUES (?,?)";

        List<Object[]> genresIds = new ArrayList<>();

        for(int i=0; i<film.getGenres().size(); i++){
            genresIds.add(new Object[]{film.getId(),film.getGenres().get(i).getId()});
            log.info("Added genre: "+film.getId()+" "+film.getGenres().get(i).getId());
        }
        try{
            jdbcTemplate.batchUpdate(sql,genresIds);
        }
        catch(Exception e){
            throw new RuntimeException("Unable to insert genres",e);
        }
    }

    @Override
    public Map<Integer, Film> getFilmsList()
    {
        String sql = "SELECT films.id, " +
                            "films.title, " +
                            "films.description, " +
                            "films.release_date, " +
                            "films.duration, " +
                            "films.likes_count, " +
                            "mpa_rating.rating FROM films LEFT OUTER JOIN " +
                            "mpa_rating on films.mpa_rating_id =mpa_rating.id";

        try{
            Map<Integer, Film> filmsList = jdbcTemplate.query(sql,new FilmRowMapper())
                    .stream()
                    .collect(Collectors.toMap(Film::getId,
                            film->film,
                            (existing,replacement)->existing));

            Map<Integer, List<FilmGenre>> filmsGenres=getFilmsGenres();

            for(Film film : filmsList.values())
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
    public Film getFilmById(int id) throws NonExistingFilmException
    {
        if(!checkFilmExists(id))
        {
            throw new NonExistingFilmException(id);
        }
        String sql="SELECT * FROM films WHERE id = ?";

        String genresSql="SELECT genre_id FROM film_genres WHERE film_id = ?";

        try{
            Film film=jdbcTemplate.queryForObject(sql,new FilmRowMapper(),id);
            ArrayList<Integer> genresIds=new ArrayList<Integer>(jdbcTemplate.queryForList(genresSql,Integer.class,film.getId()));
            film.setMpaRating(getMPARatingByFilmId(id));
            film.setGenres(getGenresById(genresIds));
            return film;
        }
        catch(Exception e){
            log.error("Cannot get film by id "+id,e);
            throw new RuntimeException("Unable to retrieve film by id "+id,e);
        }
    }

    public int insertFilm(Film film){

        String sql="INSERT INTO films (title,description,release_date,duration,likes_count,mpa_rating_id) " +
                "VALUES (?,?,?,?,?,?) RETURNING id";

        try{
            int generatedID=jdbcTemplate.queryForObject(sql,
                    Integer.class,
                    film.getTitle(),
                    film.getDescription(),
                    film.getReleaseDate(),
                    convertToInterval(film.getDuration()),
                    film.getLikesCount(),
                    film.getMpaRating().getId());
            return generatedID;
        }
        catch(Exception e){
            log.error("Cannot insert film "+film,e);
            throw new RuntimeException("Unable to insert film "+film,e);
        }
    }

    @Override
    @Transactional
    public void addFilm(Film film) throws FilmAlreadyExistsException, GenresNotExistException
    {
        if(checkFilmExists(film.getTitle(),film.getReleaseDate()))
        {
            throw new FilmAlreadyExistsException(film.getTitle(),film.getReleaseDate());
        }

        try{
            int generatedId=insertFilm(film);
            film.setId(generatedId);
            insertFilmsGenresIds(film);
            log.info("Film "+film.getTitle()+" added");
        }
        catch(Exception e) {
            log.error("Cannot add film " + film.getTitle(), e);
            throw new RuntimeException("Unable to add film " + film.getTitle(), e);
        }
    }

    @Override
    public void removeFilm(int filmId)
    {
        String sql="DELETE FROM films WHERE id = ?";

        if(!checkFilmExists(filmId))
        {
            log.info("Film "+filmId+" not found");
            throw new NonExistingFilmException(filmId);
        }

        try{
            removeFilmGenres(filmId);
            jdbcTemplate.update(sql,filmId);
            log.info("Film "+filmId+" removed");
        }
        catch(Exception e){
            log.error("Cannot remove film "+filmId,e);
            throw new RuntimeException("Unable to remove film "+filmId,e);
        }
    }

    public void removeFilmGenres(int filmId)
    {
        String sql="DELETE FROM film_genres WHERE film_id = ?";

        try{
            jdbcTemplate.update(sql,filmId);
            log.info("Film genres id="+filmId+" removed");
        }
        catch(Exception e){
            log.error("Cannot remove film genres "+filmId,e);
            throw new RuntimeException("Unable to remove film genres "+filmId,e);
        }
    }

    @Override
    public void updateFilm(int filmId, Film film)
    {
        film.setId(filmId);
        String sql="UPDATE films SET title=?, " +
                "description=?, " +
                "release_date=?, " +
                "duration=?, " +
                "likes_count=?, " +
                "mpa_rating_id=? " +
                "WHERE id=?";
        try{
            jdbcTemplate.update(sql,film.getTitle(),
                    film.getDescription(),
                    film.getReleaseDate(),
                    convertToInterval(film.getDuration()),
                    film.getLikesCount(),
                    film.getMpaRating().getId(),
                    filmId);
            log.info("Film "+film.getTitle()+" updated");

            updateFilmGenres(filmId,film);
        }
        catch(Exception e){
            log.error("Cannot update film "+filmId,e);
            throw new RuntimeException("Unable to update film "+filmId,e);
        }
    }

    public void updateFilmGenres(int filmId, Film film)
    {
        try{
            removeFilmGenres(filmId);
            insertFilmsGenresIds(film);
            log.info("Film genres id="+filmId+" updated");
        }
        catch(Exception e){
            log.error("Cannot update film genres "+filmId,e);
            throw new RuntimeException("Unable to update film genres "+filmId,e); //доделать лайки
        }
    }

    private ArrayList<FilmGenre> getGenresById(ArrayList<Integer> ids)
    {
        String idString=buildGenresString(ids);

        String sql="SELECT * FROM genres WHERE id IN " + idString;

        ArrayList<FilmGenre> genres=null;
        try{
            genres= new ArrayList<>(jdbcTemplate.query(sql,new FilmGenreMapper()));
        }
        catch(Exception e){
            log.error("Cannot get genres",e);
            throw new RuntimeException("Cannot get genres",e);
        }

        if(genres.size()!=ids.size())
        {
            Set<Integer> foundIds=genres.stream().map(FilmGenre::getId).collect(Collectors.toSet());
            List<Integer> missingIds = ids.stream()
                    .filter(id->!foundIds.contains(id))
                    .collect(Collectors.toList());
            throw new GenresNotExistException(buildGenresString(missingIds));
        }

        log.info("Found "+genres.size()+" genres");
        return genres;

    }

    private MPARating getMPARatingByFilmId(int filmId) {
        String sql = "SELECT mpa_rating.id, " +
                "mpa_rating.rating FROM films " +
                "LEFT OUTER JOIN mpa_rating ON films.mpa_rating_id=mpa_rating.id " +
                "WHERE films.id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new MPARatingMapper(), filmId);
        } catch (Exception e) {
            log.error("Cannot get MPARating: ", e);
            throw new RuntimeException("Cannot get MPARating: ", e);
        }
    }
    private MPARating getMPARatingById(int ratingId)
    {
        String sql="SELECT * FROM mpa_rating WHERE id = ?";

        try{
            return jdbcTemplate.queryForObject(sql, new MPARatingMapper(), ratingId);
        }
        catch(Exception e){
            log.error("Cannot get MPARating: ", e);
            throw new RuntimeException("Cannot get MPARating: ", e);
        }
    }

    public Film convertToFilm(FilmRequest filmRequest) throws GenresNotExistException
    {
        Film film = new Film();

        film.setId(filmRequest.getId());
        film.setTitle(filmRequest.getTitle());
        film.setDescription(filmRequest.getDescription());
        film.setReleaseDate(filmRequest.getReleaseDate());
        film.setDuration(filmRequest.getDuration());
        film.setLikesCount(filmRequest.getLikesCount());
        try
        {
            film.setGenres(getGenresById(filmRequest.getGenresIDs()));
        }
        catch(GenresNotExistException e)
        {
            throw e;
        }
        film.setMpaRating(getMPARatingById(filmRequest.getMpaRatingId()));

        return film;
    }

    private PGInterval convertToInterval(Duration duration){
        long seconds=duration.getSeconds();

        long hours=seconds/3600;
        long minutes=(seconds-hours*3600)/60;
        long secs=seconds%60;

        return new PGInterval(0,0,0,(int)hours,(int)minutes,secs);
    }
    private boolean checkFilmExists(String title, LocalDate releaseDate)
    {
        String sql="SELECT COUNT(*) FROM films WHERE title LIKE ? AND release_date = ?";
        log.info("Checking film exists "+title);
        try{
            int count = jdbcTemplate.queryForObject(sql,Integer.class,title,releaseDate);
            return count > 0;
        }
        catch(Exception e){
            log.error("Cannot check existing film: ",e);
            return false;
        }
    }
    private boolean checkFilmExists(int filmId)
    {
        String sql="SELECT COUNT(*) FROM films WHERE id = ?";
        log.info("Checking film exists with id="+filmId);
        try{

            int count = jdbcTemplate.queryForObject(sql,Integer.class,filmId);
            return count > 0;
        }
        catch(Exception e){
            log.error("Cannot check existing film: ",e);
            return false;
        }
    }

    private String buildGenresString(List<Integer> genresId)
    {
        String idString="(";
        for(int i=0;i<genresId.size();i++)
        {
            if(i!=genresId.size()-1)
            {
                idString+=genresId.get(i)+",";
            }
            else
            {
                idString+=genresId.get(i)+")";
            }
        }
        return idString;
    }


}//сделать проверки

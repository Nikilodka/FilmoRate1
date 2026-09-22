package com.example.filmorate1.Controllers;

import com.example.filmorate1.BacisClasses.Film.Film;
import com.example.filmorate1.BacisClasses.Film.FilmRequest;
import com.example.filmorate1.Exceptions.FilmExceptions.FilmAlreadyExistsException;
import com.example.filmorate1.Exceptions.FilmExceptions.NonExistingFilmException;
import com.example.filmorate1.Exceptions.GenresException.GenresNotExistException;
import com.example.filmorate1.Exceptions.LikeExceptions.LikeAlreadyExistsException;
import com.example.filmorate1.Exceptions.LikeExceptions.NonExistingLikeException;
import com.example.filmorate1.Exceptions.UserExceptions.NonExistingUserException;
import com.example.filmorate1.Services.Films.FilmDBService;
import com.example.filmorate1.Services.Films.FilmService;
import com.example.filmorate1.Services.Films.InMemoryFilmService;
import com.example.filmorate1.Storages.Films.DAO.FilmDBStorage;
import com.example.filmorate1.Storages.Films.FilmStorage;
import com.example.filmorate1.Storages.Films.InMemoryFilmStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Component
@RestController
@Slf4j
public class FilmController {
    private final FilmDBStorage filmDBStorage;
    private final FilmDBService filmDBService;

    private FilmStorage inMemoryFilmStorage;
    private InMemoryFilmService inMemoryFilmService;
    private FilmStorage dbFilmStorage;
    private FilmService dbFilmService;

    private boolean validFilmCache;

    @Autowired
    public FilmController(InMemoryFilmStorage inMemoryFilmStorage, InMemoryFilmService inMemoryFilmService,
                          FilmDBStorage dbFilmStorage, FilmDBService dbFilmService, FilmDBStorage filmDBStorage, FilmDBService filmDBService) {

        this.inMemoryFilmStorage = inMemoryFilmStorage;
        this.inMemoryFilmService = inMemoryFilmService;
        this.dbFilmStorage = dbFilmStorage;
        this.dbFilmService = dbFilmService;

        validFilmCache = false;

        this.filmDBStorage = filmDBStorage;
        this.filmDBService = filmDBService;
    }


    @GetMapping("/films")
    public ResponseEntity<?> getFilms() { // checked
        log.info("Получен запрос GetFilms");
        try{
            if(validFilmCache ==false){
                inMemoryFilmStorage.setFilmsList(dbFilmStorage.getFilmsList());
                validFilmCache = true;
            }
            log.info("Valid GetFilms");
            return new ResponseEntity<>(inMemoryFilmStorage.getFilmsList(), HttpStatus.OK);
        }
        catch (Exception e){
            log.error("Unable to get filmsList: "+e.getMessage());
            return new ResponseEntity<>("Unable to get filmsList: "+e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/films")
    public ResponseEntity<?> postFilm(@RequestBody @Validated FilmRequest filmRequest) { //checked
        log.info("Получен запрос PutFilm");
        Film film=null;

        try {
            film = filmDBStorage.convertToFilm(filmRequest);
            filmDBStorage.addFilm(film);
            log.info("Film added successfully");
            validFilmCache = false;
            return ResponseEntity.status(HttpStatus.CREATED).body(film);
        } catch(GenresNotExistException e){
            log.error("Cannot add film: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(filmRequest);
        } catch (FilmAlreadyExistsException e) {
            log.warn("Cannot add film: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(film);
        }

    }

    @PutMapping("/films/{filmId}")
    public ResponseEntity<?> putFilm(@RequestBody @Validated FilmRequest filmRequest, // checked
                                        @PathVariable int filmId) {
        log.info("Получен запрос PutFilm");
        Film film = null;

        try {
            film=filmDBStorage.convertToFilm(filmRequest);
            filmDBStorage.updateFilm(filmId, film);
            log.info("Фильм с ID={} успешно обновлен", film.getId());
            validFilmCache = false;
            return ResponseEntity.status(HttpStatus.OK).body(film);
        } catch (NonExistingFilmException e) {
            log.info("Cannot update film: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(film);
        } catch (GenresNotExistException e) {
            log.warn("Cannot update film: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(filmRequest);
        }
    }

    @DeleteMapping("/films/{filmId}")
    public ResponseEntity<?> deleteFilm(@PathVariable int filmId) {//checked
        try{
            filmDBStorage.removeFilm(filmId);
            validFilmCache = false;
            log.info("Film deleted successfully");
            return ResponseEntity.status(HttpStatus.OK).body("Film has been deleted successfully");
        }
        catch (NonExistingFilmException e) {
            log.info("Cannot delete film: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/films/like")
    public ResponseEntity<String> addLike(@RequestParam int filmId, // checked
                                          @RequestParam int userId) {
        log.info("Adding like to the film with ID: " + filmId);

        try{
            filmDBService.addLike(userId, filmId);
            log.info("Like added successfully to the film with ID: " + filmId);
            return ResponseEntity.status(HttpStatus.OK).body("Like has been given");
        }
        catch (NonExistingFilmException e) {
            log.warn("Cannot add like to the film with ID={}: " + e.getMessage(),filmId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        catch (NonExistingUserException e) {
            log.warn("Cannot add like by user with ID={}: " + e.getMessage(),userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        catch (LikeAlreadyExistsException e)
        {
            log.error("Cannot add like to film "+filmId+" :"+e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }


    @DeleteMapping("/films/like")
    public ResponseEntity<?> removeLike(@RequestParam int filmId, // checked
                                         @RequestParam int userId) {
        log.info("Removing like from the film with ID: " + filmId);

        try{
            filmDBService.removeLike(userId, filmId);
            log.info("Like removed successfully from the film with ID: " + filmId);
            return ResponseEntity.status(HttpStatus.OK).body("Like has been removed ");
        }
        catch (NonExistingFilmException e) {
            log.warn("Cannot remove like from the film with ID={}: " + e.getMessage(),filmId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        catch (NonExistingUserException e) {
            log.warn("Cannot remove like from user with ID={}: " + e.getMessage(),userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        catch(NonExistingLikeException e)
        {
            log.warn("Cannot remove like from user with ID={}: " + e.getMessage(),userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    @GetMapping("/films/like")
    public ResponseEntity<?> getMostLikedFilms() { // checked
        log.info("Get most liked films");
        try{
            List<Film> films = filmDBService.getMostLikedFilms();
            log.info("Most liked films");
            return ResponseEntity.status(HttpStatus.OK).body(films);
        }
        catch (NonExistingFilmException e) {
            log.info("Cannot get most liked films");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    @GetMapping("/films/{id}")
    public ResponseEntity<?> getFilmById(@PathVariable int id) {// checked

        log.info("Get film by ID: " + id);

        try{
            Film film=null;
            if(validFilmCache){
                film=inMemoryFilmStorage.getFilmById(id);
            }
            else {
                film=filmDBStorage.getFilmById(id);
            }
            log.info("Found film with ID=" + film.getId());
            return ResponseEntity.status(HttpStatus.OK).body(film);
        }

        catch (NonExistingFilmException e) {
            log.warn("Cannot find film with ID=" + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}

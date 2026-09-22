package com.example.filmorate1.BacisClasses.Film;

import com.example.filmorate1.Validators.ReleaseDate.ReleaseDateRange;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.hibernate.validator.constraints.time.DurationMin;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;

@Data
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Film {
    @NonNull
    @JsonIgnore
    private int id;

    @NonNull
    @NotBlank
    private String title;

    @Size(min=0,max=200)
    private String description;

    @ReleaseDateRange
    private LocalDate releaseDate;

    @DurationMin(minutes=0)
    private Duration duration;

    private int likesCount=0;

    @NonNull
    private ArrayList<FilmGenre> genres;

    @NonNull
    private MPARating mpaRating;

    public Film() {
        id=0;
        genres=new ArrayList<>();
    }
    public void addLike(){
        likesCount++;
    }
    public void removeLike(){
        likesCount--;
    }
}

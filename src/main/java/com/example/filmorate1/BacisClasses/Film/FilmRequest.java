package com.example.filmorate1.BacisClasses.Film;

import com.example.filmorate1.Validators.ReleaseDate.ReleaseDateRange;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.hibernate.validator.constraints.time.DurationMin;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;

@Getter
@Setter
public class FilmRequest {
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
    private ArrayList<Integer> genresIDs;

    @NonNull
    private int mpaRatingId;

    public FilmRequest() {id=0;}




}

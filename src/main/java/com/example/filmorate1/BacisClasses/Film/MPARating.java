package com.example.filmorate1.BacisClasses.Film;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public class MPARating {
    @NonNull
    private int id;

    @NonNull
    @NotBlank
    private String rating;
}

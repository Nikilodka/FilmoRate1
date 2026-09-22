package com.example.filmorate1.BacisClasses;

import com.example.filmorate1.Validators.BirthdayDate.BirtdayDateRange;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Getter
@Setter
public class User {
    @NonNull
    @JsonIgnore
    private int id=0;

    @NonNull
    private String name;

    @Email(message="Неккоректный e-mail!")
    private String email;
    
    @NonNull
    @NotBlank
    @Length(min=8,max=20,message = "Минимальная длина логина - 8 символов, максимальная - 20.")
    @Pattern(regexp ="\\S+",message="Логин не должен содержать пробелы")
    private String login;

    @NonNull
    @NotBlank
    @Length(min=6,max=16, message = "Длина пароля должна быть от 6 до 16 символов.")
    @Pattern(regexp ="\\S+",message="Пароль не должен содержать пробелы")
    private String password;

    @BirtdayDateRange
    private LocalDate birthdayDate;

    private Set<Integer> friendsList;

    private Set<Integer> likedFilmsList;

    public User() {
        friendsList = new HashSet<>();
        likedFilmsList = new HashSet<>();
    }

}

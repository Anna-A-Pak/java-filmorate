package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class Film {
    private int id;
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @NotBlank
    private String releaseDate;
    @Positive
    private int duration;
}

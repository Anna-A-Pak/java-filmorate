package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.ArrayList;
import java.util.List;


@Data
public class NewFilmRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @NotBlank
    private String releaseDate;
    @Positive
    private int duration;
    private Mpa mpa;
    private List<Genre> genres = new ArrayList<>();
}

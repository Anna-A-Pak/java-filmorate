package ru.yandex.practicum.filmorate.dto;

import lombok.Data;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Data
public class UpdateFilmRequest {
    private int id;
    private String name;
    private String description;
    private String releaseDate;
    private Integer duration;
    private Mpa mpa;
    private List<Genre> genres;

    public boolean hasName() {
        return ! (name == null || name.isBlank());
    }

    public boolean hasDescription() {
        return ! (description == null || description.isBlank());
    }

    public boolean hasReleaseDate() {
        return ! (releaseDate == null || releaseDate.isBlank());
    }

    public boolean hasDuration() {
        return !(duration == null);
    }

    public boolean hasMpa() {
        return !(mpa == null);
    }

    public boolean hasGenres() {
        return !(genres == null);
    }
}

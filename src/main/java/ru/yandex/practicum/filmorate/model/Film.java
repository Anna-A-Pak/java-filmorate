package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Film {
    private int id;
    private String name;
    private String description;
    private String releaseDate;
    private int duration;
    private Mpa mpa;
    private List<Genre> genres = new ArrayList<>();
}

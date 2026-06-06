package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class User {
    private int id;
    @Email
    private String email;
    private String login;
    private String name;
    private String birthday;
}

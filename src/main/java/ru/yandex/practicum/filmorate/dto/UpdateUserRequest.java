package ru.yandex.practicum.filmorate.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private int id;
    private String email;
    private String login;
    private String name;
    private String birthday;

    public boolean hasEmail() {
        return ! (email == null || email.isBlank());
    }

    public boolean hasLogin() {
        return ! (login == null || login.isBlank());
    }

    public boolean hasName() {
        return ! (name == null || name.isBlank());
    }

    public boolean hasBirthday() {
        return ! (birthday == null || birthday.isBlank());
    }
}

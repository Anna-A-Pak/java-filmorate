package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final Map<Integer, User> users = new HashMap<>();

    @PostMapping
    public User addUser(@RequestBody User user) {
        log.trace("Check user's fields");
        checkFields(user);
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.trace("Added user");
        return user;
    }

    private int getNextId() {
        int currentMaxId = users.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        log.debug("user's id: {}", currentMaxId + 1);
        return ++currentMaxId;
    }

    @PutMapping
    public User update(@Valid @RequestBody User updateUser) {
        if (updateUser.getId() == 0) {
            log.error("Error: uninitialised id");
            throw new ValidationException("Id должен быть указан");
        }
        if (users.containsKey(updateUser.getId())) {
            User oldUser = users.get(updateUser.getId());
            log.trace("Check update user's fields");
            checkFields(updateUser);
            oldUser.setEmail(updateUser.getEmail());
            oldUser.setLogin(updateUser.getLogin());
            oldUser.setName(updateUser.getName());
            oldUser.setBirthday(updateUser.getBirthday());
            log.trace("Updated user {}", oldUser.getLogin());
            return oldUser;
        }
        log.error("Error: user with id {} isn't found", updateUser.getId());
        throw new NotFoundException("Пользователь с id = " + updateUser.getId() + " не найден");
    }

    @GetMapping
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    private void checkFields(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.error("Error: uninitialised user's email");
            throw new ValidationException("Email не может быть пустым");
        }
        if (!user.getEmail().contains("@")) {
            log.error("Error: incorrect email");
            throw new ValidationException("Email должен содержать символ '@'");
        }
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.error("Error: uninitialised user's login");
            throw new ValidationException("Логин не может быть пустым");
        }
        if (user.getLogin().contains(" ")) {
            log.error("Error: incorrect login");
            throw new ValidationException("Логин не должен содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getBirthday() == null || user.getBirthday().isBlank()) {
            log.error("Error: uninitialised user's birthday");
            throw new ValidationException("Дата рождения не может быть пустой");
        }
        LocalDate birthdayUser = LocalDate.parse(user.getBirthday(), FORMATTER);
        if (birthdayUser.isAfter(LocalDate.now())) {
            log.error("Error: incorrect birthday");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}

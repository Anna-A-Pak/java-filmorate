package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final Map<Integer, User> users = new HashMap<>();

    public User addUser(User user) {
        log.debug("Check user's fields");
        checkFields(user);
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.debug("Added user");
        return user;
    }

    public User update(User updateUser) {
        if (updateUser.getId() == 0) {
            log.error("Error: uninitialised id");
            throw new ValidationException("Id должен быть указан");
        }
        if (users.containsKey(updateUser.getId())) {
            User oldUser = users.get(updateUser.getId());
            log.debug("Check update user's fields");
            checkFields(updateUser);
            oldUser.setEmail(updateUser.getEmail());
            oldUser.setLogin(updateUser.getLogin());
            oldUser.setName(updateUser.getName());
            oldUser.setBirthday(updateUser.getBirthday());
            log.debug("Updated user {}", oldUser.getLogin());
            return oldUser;
        }
        log.error("Error: user with id {} isn't found", updateUser.getId());
        throw new NotFoundException("Пользователь с id = " + updateUser.getId() + " не найден");
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public void deleteUser(Integer id) {
        users.remove(id);
    }

    public Optional<User> findById(int userId) {
        return Optional.ofNullable(users.get(userId));
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

    private void checkFields(User user) {
        if (user.getLogin().contains(" ")) {
            log.error("Error: incorrect login");
            throw new ValidationException("Логин не должен содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        LocalDate birthdayUser = LocalDate.parse(user.getBirthday(), FORMATTER);
        if (birthdayUser.isAfter(LocalDate.now())) {
            log.error("Error: incorrect birthday");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}

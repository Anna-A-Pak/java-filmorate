package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    List<User> getAllUsers();

    User addUser(User user);

    User update(User user);

    void deleteUser(Integer id);

    Optional<User> findById(int userId);
}

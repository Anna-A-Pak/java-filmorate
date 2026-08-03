package ru.yandex.practicum.filmorate.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.event.EventService;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;
    private final EventService eventService;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage,
                       EventService eventService) {
        this.userStorage = userStorage;
        this.eventService = eventService;
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User addUser(NewUserRequest request) {
        User user = UserMapper.mapToUser(request);
        checkFields(user);
        User createdUser = userStorage.addUser(user);
        log.debug("Added user with id={}", createdUser.getId());
        return createdUser;
    }

    public User update(UpdateUserRequest request) {
        if (request.getId() == 0) {
            log.error("Error: uninitialised id");
            throw new ValidationException("Id должен быть указан");
        }
        User updatedUser = userStorage.findById(request.getId())
                .map(user -> UserMapper.updateUserFields(user, request))
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        return userStorage.update(updatedUser);
    }

    public void deleteUser(Integer id) {
        userStorage.deleteUser(id);
    }

    @Transactional
    public void addFriend(Integer userId, Integer friendId) {
        User user = getUser(userId);
        User friend = getUser(friendId);

        userStorage.addFriend(userId, friendId);
        eventService.addEvent(userId, EventType.FRIEND, Operation.ADD, friendId);
    }

    @Transactional
    public void deleteFriend(Integer userId, Integer friendId) {
        User user = getUser(userId);
        User friend = getUser(friendId);

        userStorage.deleteFriend(user, friend);
        eventService.addEvent(userId, EventType.FRIEND, Operation.REMOVE, friendId);
    }

    public List<User> getAllFriends(Integer userId) {
        User user = getUser(userId);
        return userStorage.getAllFriends(userId);
    }

    public List<User> getSameFriends(Integer userId, Integer friendId) {
        User user = getUser(userId);
        User friend = getUser(friendId);

        return userStorage.getSameFriends(user, friend);
    }

    public List<Film> getRecommendations(Integer id) {
        return userStorage.getRecommendations(id);
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

    public User getUser(Integer userId) {
        Optional<User> userOptional = userStorage.findById(userId);
        if (userOptional.isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        return userOptional.get();
    }
}
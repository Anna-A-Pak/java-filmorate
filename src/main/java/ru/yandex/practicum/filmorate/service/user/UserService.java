package ru.yandex.practicum.filmorate.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User addUser(User user) {
        log.debug("Check user's fields");
        checkFields(user);
        User createdUser = userStorage.addUser(user);
        log.debug("Added user");
        return createdUser;
    }

    public User update(User user) {
        if (user.getId() == 0) {
            log.error("Error: uninitialised id");
            throw new ValidationException("Id должен быть указан");
        }
        if (userStorage.findById(user.getId()).isPresent()) {
            log.debug("Check update user's fields");
            checkFields(user);
            User updateUser = userStorage.update(user);
            log.debug("Updated user {}", updateUser.getLogin());
            return updateUser;
        }
        log.error("Error: user with id {} isn't found", user.getId());
        throw new NotFoundException("Пользователь с id = " + user.getId() + " не найден");
    }

    public void deleteUser(Integer id) {
        userStorage.deleteUser(id);
    }

    public void addFriend(Integer userId, Integer friendId) {
        User user = getUser(userId);
        User friend = getUser(friendId);

        user.getFriends().add(friend.getId());
        log.debug("User {} added as a friend {}", userId, friendId);
        friend.getFriends().add(user.getId());
        log.debug("User {} added as a friend to {}", userId, friendId);
    }

    public void deleteFriend(Integer userId, Integer friendId) {
        User user = getUser(userId);
        User friend = getUser(friendId);

        user.getFriends().remove(friend.getId());
        log.debug("Friend {} removed from user's {} friends", friendId, userId);
        friend.getFriends().remove(user.getId());
        log.debug("User {} removed from friend's {} friends", userId, friendId);
    }

    public List<User> getAllFriends(Integer userId) {
        User user = getUser(userId);
        Set<Integer> friendsId = user.getFriends();
        log.debug("Getting a list of friends");
        return friendsId.stream()
                .map(this::getUser)
                .toList();
    }

    public List<User> getSameFriends(Integer userId, Integer friendId) {
        User user = getUser(userId);
        Set<Integer> friendsUser = user.getFriends();

        User friend = getUser(friendId);
        Set<Integer> friendsFriend = friend.getFriends();

        log.debug("Getting a list of identical friends");
        return friendsUser.stream()
                .filter(friendsFriend::contains)
                .map(this::getUser)
                .toList();
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

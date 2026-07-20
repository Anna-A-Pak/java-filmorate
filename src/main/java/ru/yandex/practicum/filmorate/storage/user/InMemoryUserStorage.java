package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Integer, User> users = new HashMap<>();

    public User addUser(User user) {
        user.setId(getNextId());
        users.put(user.getId(), user);
        return user;
    }

    public User update(User user) {
        User updateUser = users.get(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setLogin(user.getLogin());
        updateUser.setName(user.getName());
        updateUser.setBirthday(user.getBirthday());
        return updateUser;
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

    public void addFriend(Integer userId, Integer friendId) {
        User user = findById(userId).get();
        User friend = findById(friendId).get();
        user.getFriends().add(friend.getId());
        log.debug("User {} added as a friend {}", user.getId(), friend.getId());
        friend.getFriends().add(user.getId());
        log.debug("User {} added as a friend to {}", user.getId(), friend.getId());
    }

    public void deleteFriend(User user, User friend) {
        user.getFriends().remove(friend.getId());
        log.debug("Friend {} removed from user's {} friends", friend.getId(), user.getId());
        friend.getFriends().remove(user.getId());
        log.debug("User {} removed from friend's {} friends", user.getId(), friend.getId());
    }

    public List<User> getAllFriends(Integer userId) {
        User user = findById(userId).get();
        Set<Integer> friendsId = user.getFriends();
        log.debug("Getting a list of friends");
        return friendsId.stream()
                .map(id -> findById(id)
                        .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден")))
                .toList();
    }

    public List<User> getSameFriends(User user, User friend) {
        Set<Integer> friendsUser = user.getFriends();
        Set<Integer> friendsFriend = friend.getFriends();

        log.debug("Getting a list of identical friends");
        return friendsUser.stream()
                .filter(friendsFriend::contains)
                .map(id -> findById(id)
                        .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден")))
                .toList();
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
}

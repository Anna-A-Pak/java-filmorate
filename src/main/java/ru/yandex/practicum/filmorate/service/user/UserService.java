package ru.yandex.practicum.filmorate.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User addUser(User user) {
        return userStorage.addUser(user);
    }

    public User update(User user) {
        return userStorage.update(user);
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
        Set<Integer> friends = user.getFriends();
        List<User> users = userStorage.getAllUsers();
        log.debug("Getting a list of friends");
        return users.stream()
                .filter(u -> friends.contains(u.getId()))
                .toList();
    }

    public List<User> getSameFriends(Integer userId, Integer friendId) {
        User user = getUser(userId);
        Set<Integer> friendsUser = user.getFriends();

        User friend = getUser(friendId);
        Set<Integer> friendsFriend = friend.getFriends();

        List<User> users = userStorage.getAllUsers();
        log.debug("Getting a list of identical friends");
        return users.stream()
                .filter(u -> friendsUser.contains(u.getId()))
                .filter(u -> friendsFriend.contains(u.getId()))
                .toList();
    }

    public User getUser(Integer userId) {
        Optional<User> userOptional = userStorage.findById(userId);
        if (userOptional.isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        return userOptional.get();
    }
}

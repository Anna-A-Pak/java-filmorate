package ru.yandex.practicum.filmorate.dal;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;


import java.util.List;
import java.util.Optional;

@Repository
public class UserDbStorage extends BaseStorage implements UserStorage {

    private final UserRowMapper mapper;

    public UserDbStorage(JdbcTemplate jdbc, UserRowMapper mapper) {
        super(jdbc);
        this.mapper = mapper;
    }

    private static final String GET_ALL_QUERY = """
            SELECT
              *
            FROM users""";

    private static final String INSERT_QUERY = """
            INSERT INTO
              users (email, login, user_name, birthday)
            VALUES
              (?, ?, ?, ?)""";

    private static final String UPDATE_QUERY = """
            UPDATE users
            SET
              email = ?,
              login = ?,
              user_name = ?,
              birthday = ?
            WHERE
              user_id = ?""";

    private static final String DELETE_QUERY = """
            DELETE FROM users
            WHERE
              user_id = ?""";

    private static final String FIND_BY_ID_QUERY = """
            SELECT
              *
            FROM
              users
            WHERE
              user_id = ?""";

    private static final String INSERT_FRIEND_QUERY = """
            INSERT INTO
              friendship (user_id, friend_id)
            VALUES
              (?, ?)""";

    private static final String DELETE_FRIEND_QUERY = """
            DELETE FROM friendship
            WHERE
              user_id = ?
              AND friend_id = ?""";

    private static final String GET_ALL_FRIENDS_QUERY = """
            SELECT
              u.user_id,
              u.email,
              u.login,
              u.user_name,
              u.birthday
            FROM
              friendship AS f
              JOIN users AS u ON f.friend_id = u.user_id
            WHERE
              f.user_id = ?""";

    private static final String GET_SAME_FRIENDS = """
            SELECT
              u.user_id,
              u.email,
              u.login,
              u.user_name,
              u.birthday
            FROM
              friendship AS f
              JOIN users AS u ON f.friend_id = u.user_id
            WHERE
              f.user_id = ?
              AND f.friend_id IN (
                SELECT
                  fs.friend_id
                FROM
                  friendship AS fs
                WHERE
                  fs.user_id = ?
              )""";

    public List<User> getAllUsers() {
        return jdbc.query(GET_ALL_QUERY, mapper);
    }

    public User addUser(User user) {
        int id = insert(
                INSERT_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday()
        );
        user.setId(id);
        return user;
    }

    public User update(User user) {
        update(
                UPDATE_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );
        return user;
    }

    @Override
    public void deleteUser(Integer id) {
        int rows = jdbc.update(DELETE_QUERY, id);

        if (rows == 0) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
    }

    public Optional<User> findById(int userId) {
        try {
            User result = jdbc.queryForObject(FIND_BY_ID_QUERY, mapper, userId);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    public void addFriend(Integer userId, Integer friendId) {
        jdbc.update(INSERT_FRIEND_QUERY, userId, friendId);
    }

    public void deleteFriend(User user, User friend) {
        jdbc.update(DELETE_FRIEND_QUERY, user.getId(), friend.getId());
    }

    public List<User> getAllFriends(Integer userId) {
        return jdbc.query(GET_ALL_FRIENDS_QUERY, (rs, rowNum) -> {
            User friend = new User();
            friend.setId(rs.getInt("user_id"));
            friend.setEmail(rs.getString("email"));
            friend.setLogin(rs.getString("login"));
            friend.setName(rs.getString("user_name"));
            friend.setBirthday(rs.getString("birthday"));

            return friend;
        }, userId);
    }

    public List<User> getSameFriends(User user, User friend) {
        return jdbc.query(GET_SAME_FRIENDS, (rs, rowNum) -> {
            User sameFriend = new User();
            sameFriend.setId(rs.getInt("user_id"));
            sameFriend.setEmail(rs.getString("email"));
            sameFriend.setLogin(rs.getString("login"));
            sameFriend.setName(rs.getString("user_name"));
            sameFriend.setBirthday(rs.getString("birthday"));

            return sameFriend;
        }, user.getId(), friend.getId());
    }
}

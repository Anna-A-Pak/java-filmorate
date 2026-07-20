package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbc;
    private final UserRowMapper mapper;

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
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );
        return user;
    }

    public void deleteUser(Integer id) {
        jdbc.update(DELETE_QUERY, id);
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

    protected int insert(Object... params) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(UserDbStorage.INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
            for (int idx = 0; idx < params.length; idx++) {
                ps.setObject(idx + 1, params[idx]);
            }
            return ps; }, keyHolder);

        Integer id = keyHolder.getKeyAs(Integer.class);

        if (id != null) {
            return id;
        } else {
            throw new InternalServerException("Не удалось сохранить данные");
        }
    }

    protected void update(Object... params) {
        int rowsUpdated = jdbc.update(UserDbStorage.UPDATE_QUERY, params);
        if (rowsUpdated == 0) {
            throw new InternalServerException("Не удалось обновить данные");
        }
    }
}

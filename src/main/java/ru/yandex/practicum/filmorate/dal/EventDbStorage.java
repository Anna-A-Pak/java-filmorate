package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.EventRowMapper;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;

import java.util.List;

@Repository
public class EventDbStorage extends BaseStorage implements EventStorage {
    private final EventRowMapper mapper;

    public EventDbStorage(JdbcTemplate jdbc, EventRowMapper mapper) {
        super(jdbc);
        this.mapper = mapper;
    }

    private static final String INSERT_QUERY = """
            INSERT INTO
              events (time_stamp, user_id, event_type, operation, entity_id)
            VALUES
              (?, ?, ?, ?, ?)""";

    private static final String FIND_BY_USER_ID_QUERY = """
            SELECT
              *
            FROM
              events
            WHERE
              user_id = ?""";

    @Override
    public void addEvent(Event event) {
        /*int id =*/ insert(
                INSERT_QUERY,
                event.getTimestamp(),
                event.getUserId(),
                event.getEventType(),
                event.getOperation(),
                event.getEntityId()
        );
        //event.setEventId(id);
    }

    @Override
    public List<Event> getEvents(Integer userId) {
        return jdbc.query(FIND_BY_USER_ID_QUERY, mapper, userId);
    }
}

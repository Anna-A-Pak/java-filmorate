package ru.yandex.practicum.filmorate.service.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;

import java.time.Instant;

@Service
public class EventService {
    private final EventStorage eventStorage;

    @Autowired
    public EventService(EventStorage eventStorage) {
        this.eventStorage = eventStorage;
    }

    public void addEvent(Integer userId, EventType eventType, Operation operation, Integer entityId) {
        Event event = new Event();
        event.setTimestamp(Instant.now().getEpochSecond());
        event.setUserId(userId);
        event.setEventType(eventType);
        event.setOperation(operation);
        event.setEntityId(entityId);

        eventStorage.addEvent(event);
    }
}

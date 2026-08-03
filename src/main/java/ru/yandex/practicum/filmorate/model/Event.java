package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Event {
    private Integer eventId;
    private Long timestamp;
    private Integer userId;
    private EventType eventType;
    private Operation operation;
    private Integer entityId;
}

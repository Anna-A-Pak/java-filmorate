package ru.yandex.practicum.filmorate.dto;

import lombok.Data;

@Data
public class UpdateReviewRequest {
    private Integer reviewId;
    private String content;
    private Boolean isPositive;

    public boolean hasContent() {
        return !(content == null || content.isBlank());
    }

    public boolean hasIsPositive() {
        return !(isPositive == null);
    }
}

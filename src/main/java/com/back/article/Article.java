package com.back.article;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Article {
    private Long id;
    private String title;
    private String body;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private Boolean isBlind;

    public Boolean isBlind() {
        return isBlind;
    }
}

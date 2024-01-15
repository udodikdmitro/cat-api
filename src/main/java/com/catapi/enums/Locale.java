package com.catapi.enums;

import lombok.Getter;

@Getter
public enum Locale {

    UA("Ukrainian"),
    RU("Russian");
    private final String languageName;

    Locale(String languageName) {
        this.languageName = languageName;
    }
}

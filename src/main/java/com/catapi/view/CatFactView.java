package com.catapi.view;

import java.util.Objects;

public record CatFactView(
        String fact,
        int length
) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CatFactView that = (CatFactView) o;
        return length == that.length && Objects.equals(fact, that.fact);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fact, length);
    }
}
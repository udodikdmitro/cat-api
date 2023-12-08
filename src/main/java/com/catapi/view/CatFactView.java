package com.catapi.view;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CatFactView {

    private String fact;
    private int length;

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

    @Override
    public String toString() {
        return "CatFactView{" +
                "fact='" + fact + '\'' +
                ", length=" + length +
                '}';
    }
}

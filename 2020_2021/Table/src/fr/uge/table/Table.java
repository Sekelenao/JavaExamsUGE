package fr.uge.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Table<T> {

    private final List<T> elements;

    private final boolean isDynamic;

    private Table(List<T> elements, boolean isDynamic) {
        this.elements = elements;
        this.isDynamic = isDynamic;
    }

    private static <T> List<T> arrayToList(T... elements) {
        var lst = new ArrayList<T>(elements.length);
        for (var element : elements) {
            Objects.requireNonNull(element);
            lst.add(element);
        }
        return lst;
    }

    public static <T> Table<T> of(T... elements) {
        Objects.requireNonNull(elements);
        return new Table<>(arrayToList(elements), false);
    }

    public static <T> Table<T> dynamic(T... elements) {
        Objects.requireNonNull(elements);
        return new Table<>(arrayToList(elements), true);
    }

    public int size() {
        return elements.size();
    }


}

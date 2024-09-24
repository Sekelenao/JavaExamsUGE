package fr.uge.policy;

import java.util.HashMap;
import java.util.Objects;
import java.util.function.Predicate;

public final class Policy<T> {

    private final HashMap<Object, T> elements = new HashMap<>();

    private Predicate<T> policies = _ -> false;

    public void deny(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        policies = policies.or(predicate);
    }

    public void allow(T element) {
        Objects.requireNonNull(element);
        elements.putIfAbsent(element, element);
    }

    public boolean allowed(Object element) {
        Objects.requireNonNull(element);
        var elem = elements.get(element);
        return elem != null && !policies.test(elem);
    }

    public Predicate<T> asAllDenyFilter(){
        return policies;
    }

}

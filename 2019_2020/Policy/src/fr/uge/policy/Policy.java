package fr.uge.policy;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class Policy<T> implements Iterable<T> {

    private final Map<Object, T> elements = new LinkedHashMap<>();

    private Predicate<T> policies = _ -> false;

    private int version;

    public void deny(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        policies = policies.or(predicate);
        version++;
    }

    public void allow(T element) {
        Objects.requireNonNull(element);
        elements.putIfAbsent(element, element);
        version++;
    }

    public boolean allowed(Object element) {
        Objects.requireNonNull(element);
        var elem = elements.get(element);
        return elem != null && !policies.test(elem);
    }

    public Predicate<T> asAllDenyFilter(){
        return policies;
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        Objects.requireNonNull(action);
        elements.values().stream().filter(Predicate.not(policies)).forEach(action);
    }

    public void addAll(Policy<T> other) {
        Objects.requireNonNull(other);
        elements.putAll(other.elements);
        policies = policies.or(other.policies);
        version++;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {

            private final Iterator<T> it = elements.values().iterator();

            private final int expectedVersion = version;

            private T next;

            @Override
            public boolean hasNext() {
                while (it.hasNext() && next == null) {
                    var value = it.next();
                    if(!policies.test(value)) {
                        next = value;
                    }
                }
                return next != null;
            }

            @Override
            public T next() {
                if(!hasNext()) throw new NoSuchElementException();
                if(expectedVersion != version) throw new ConcurrentModificationException();
                var value = next;
                next = null;
                return value;
            }

        };
    }

}

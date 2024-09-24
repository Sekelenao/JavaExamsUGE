package fr.uge.policy;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class Policy<T> implements Iterable<T> {

    private final Map<Object, T> elements = new LinkedHashMap<>();

    private Predicate<T> policies = _ -> false;

    private int lastVersionOfSizeCheck = 0;

    private int lastKnownSize = 0;

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

            private void checkForConcurrentModification(){
                if(expectedVersion != version) throw new ConcurrentModificationException();
            }

            @Override
            public boolean hasNext() {
                checkForConcurrentModification();
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
                checkForConcurrentModification();
                if(!hasNext()) throw new NoSuchElementException();
                var value = next;
                next = null;
                return value;
            }

        };
    }

    public Set<T> asSet(){
        return new AbstractSet<>() {

            @Override
            public Iterator<T> iterator() {
                return Policy.this.iterator();
            }

            @Override
            public int size() {
                if(lastVersionOfSizeCheck == version) return lastKnownSize;
                var size = 0;
                for(var element : elements.values()) {
                    if(!policies.test(element)) {
                        size++;
                    }
                }
                lastKnownSize = size;
                lastVersionOfSizeCheck = version;
                return size;
            }

            @Override
            public boolean contains(Object other) {
                Objects.requireNonNull(other);
                var element = elements.get(other);
                return element != null && !policies.test(element);
            }

        };
    }

    @Override
    public Spliterator<T> spliterator(){
        return new Spliterator<>() {

            private final Iterator<T> it = Policy.this.iterator();

            private int size = lastVersionOfSizeCheck == version ? lastKnownSize : Integer.MAX_VALUE;

            @Override
            public boolean tryAdvance(Consumer<? super T> action) {
                Objects.requireNonNull(action);
                if(it.hasNext()) {
                    action.accept(it.next());
                    size--;
                    return true;
                }
                return false;
            }

            @Override
            public Spliterator<T> trySplit() {
                return null;
            }

            @Override
            public long estimateSize() {
                return size;
            }

            @Override
            public int characteristics() {
                var opt = ORDERED | NONNULL | DISTINCT;
                if(size != Integer.MAX_VALUE) opt |= SIZED;
                return opt;
            }

        };
    }

    public Stream<T> stream(){
        return StreamSupport.stream(spliterator(), false);
    }

}

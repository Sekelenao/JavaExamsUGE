package fr.uge.java.cord;

import java.util.*;
import java.util.stream.Stream;

public final class Cord<T> implements Iterable<T> {

    private final List<T> values = new ArrayList<>();

    private Cord<? extends T> mother;

    public Cord(){
        this(null);
    }

    private Cord(Cord<T> mother){
        this.mother = mother;
    }

    public Cord<T> createChild(){
        return new Cord<>(this);
    }

    public void add(T element){
        values.add(Objects.requireNonNull(element));
    }

    public T get(int index){
        if(index < 0 || (values.isEmpty() && mother == null)) {
            throw new IndexOutOfBoundsException();
        }
        int cursor = index;
        Cord<? extends T> currentCord = this;
        while (cursor >= currentCord.values.size()){
            if(currentCord.mother == null) throw new IndexOutOfBoundsException();
            cursor -= currentCord.values.size();
            currentCord = currentCord.mother;
        }
        return currentCord.values.reversed().get(cursor);
    }

    public void forEachIndexed(IndexedValueConsumer<? super T> action){
        Objects.requireNonNull(action);
        int i = 0;
        Cord<? extends T> current = this;
        while(current != null){
            var view = current.values.reversed();
            for(var e : view) action.accept(i++, e);
            current = current.mother;
        }
    }

    @Override
    public Iterator<T> iterator() {
        final var currentCord = this;
        return new Iterator<>() {

            private int index;
            private Cord<? extends T> current = currentCord;

            private boolean loadNextNonEmptyMother(){
                if(current == null) return false;
                current = current.mother;
                if(current == null) return false;
                while (current.values.isEmpty()){
                    var next = current.mother;
                    if(next == null) return false;
                    current = next;
                }
                index = 0;
                return true;
            }

            @Override
            public boolean hasNext() {
                if(current == null) return false;
                if(index < current.values.size()) return true;
                return loadNextNonEmptyMother();
            }

            @Override
            public T next() {
                if(!hasNext()) throw new NoSuchElementException(); // Call the loader anyway
                return current.values.reversed().get(index++);
            }
        };
    }

    public void attachParent(Cord<? extends T> parent) {
        Objects.requireNonNull(parent);
        if(mother != null) throw new IllegalStateException("Already have parent");
        var current = parent;
        while(current != null){
            if(current == this) throw new IllegalStateException("Cannot create cycle");
            current = current.mother;
        }
        this.mother = parent;
    }

    public record IndexedElement<T>(int index, T element){

        public IndexedElement {
            Objects.requireNonNull(element);
            if(index < 0) throw new IllegalArgumentException("Index cannot be negative");
        }

    }

    public Stream<IndexedElement<T>> indexedElements() {
        return Stream.of(this).mapMulti((cord, consumer) ->
                cord.forEachIndexed((i, e) -> consumer.accept(new IndexedElement<>(i, e)))
        );
    }
}
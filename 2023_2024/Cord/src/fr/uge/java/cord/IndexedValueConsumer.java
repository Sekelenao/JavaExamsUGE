package fr.uge.java.cord;

@FunctionalInterface
public interface IndexedValueConsumer<T> {
    void accept(int index, T element);
}

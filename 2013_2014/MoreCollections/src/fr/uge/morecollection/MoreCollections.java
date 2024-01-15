package fr.uge.morecollection;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;

public final class MoreCollections {

    private static final class MappedListView<E, T> extends AbstractList<E> implements RandomAccess {

        private final List<? extends T> reference;
        private final Function<? super T, ? extends E> mapper;

        public MappedListView(List<? extends T> list, Function<? super T, ? extends E> mapper){
            this.reference = Objects.requireNonNull(list);
            this.mapper = Objects.requireNonNull(mapper);
        }

        @Override
        public E get(int index) {
            Objects.checkIndex(index, reference.size());
            return mapper.apply(reference.get(index));
        }

        @Override
        public int size() {
            return reference.size();
        }

    }

    private static class MappedCollectionView<E, T> extends AbstractCollection<E> {

        private final Collection<? extends T> reference;
        private final Function<? super T, ? extends E> mapper;

        public MappedCollectionView(Collection<? extends T> collection, Function<? super T, ? extends E> mapper){
            this.reference = Objects.requireNonNull(collection);
            this.mapper = Objects.requireNonNull(mapper);
        }

        @Override
        public Iterator<E> iterator() {
            return new Iterator<>() {
                private final Iterator<? extends T> it = reference.iterator();
                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public E next() {
                    if(!hasNext()) throw new IndexOutOfBoundsException();
                    return mapper.apply(it.next());
                }

                @Override
                public void remove() {
                    it.remove();
                }
            };
        }

        @Override
        public int size() {
            return reference.size();
        }
    }

    private static final class SerializableMappedCollectionView<E, T> extends MappedCollectionView<E, T> implements Serializable {
        public SerializableMappedCollectionView(Collection<? extends T> collection, Function<? super T, ? extends E> mapper) {
            super(Objects.requireNonNull(collection), Objects.requireNonNull(mapper));
        }
    }


    private MoreCollections(){
        throw new AssertionError("You cannot instantiate this class.");
    }

    public static <T, E> List<E> asMappedList(List<? extends T> list, Function<? super T, ? extends E> mapper) {
        Objects.requireNonNull(list);
        Objects.requireNonNull(mapper);
        switch (list){
            case RandomAccess ignored -> {
                return new MappedListView<>(list, mapper);
            }
            default -> throw new IllegalStateException("Only for lists implementing RandomAccess.");
        }
    }

    public static <T, E> Collection<E> asMappedCollection(Collection<? extends T> collection, Function<? super T, ? extends E> mapper) {
        Objects.requireNonNull(collection);
        Objects.requireNonNull(mapper);
        return switch (collection){
            case Serializable ignored -> new SerializableMappedCollectionView<E, T>(collection, mapper);
            default -> new MappedCollectionView<>(collection, mapper);
        };
    }

}

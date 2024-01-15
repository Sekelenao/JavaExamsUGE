package fr.uge.morecollection;

import java.util.*;
import java.util.function.Function;

public final class MoreCollections {

    private static final class MappedView<E, T> extends AbstractList<E> implements RandomAccess {

        private final List<? extends T> reference;

        private final Function<? super T, ? extends E> mapper;

        public MappedView(List<? extends T> list, Function<? super T, ? extends E> mapper){
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

    private MoreCollections(){
        throw new AssertionError("You cannot instantiate this class.");
    }

    public static <T, E> List<E> asMappedList(List<? extends T> list, Function<? super T, ? extends E> mapper) {
        Objects.requireNonNull(list);
        Objects.requireNonNull(mapper);
        if(!(list instanceof RandomAccess)) {
            throw new IllegalStateException("Only for RandomAccess list.");
        }
        return new MappedView<>(list, mapper);
    }
}

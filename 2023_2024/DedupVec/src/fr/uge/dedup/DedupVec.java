package fr.uge.dedup;

import java.util.*;

public final class DedupVec<T> {

    private final ArrayList<T> elements = new ArrayList<>();

    private final HashMap<T, T> references = new HashMap<>();

    public void add(T element){
        Objects.requireNonNull(element);
        var existingElem = references.get(element);
        if(existingElem == null){
            elements.add(element);
            references.put(element, element);
        } else {
            elements.add(existingElem);
        }
    }

    public T get(int index){
        return elements.get(index);
    }

    public int size(){
        return elements.size();
    }

    public boolean contains(Object element){
        return references.containsKey(element);
    }

    public void addAll(DedupVec<? extends T> other){
        Objects.requireNonNull(other);
        other.elements.forEach(this::add);
    }

    static <E> Map<E, E> newMapFromSet(Set<E> set){
        return new AbstractMap<>() {

            @Override
            public Set<Entry<E, E>> entrySet() {
                return new AbstractSet<>() {

                    @Override
                    public Iterator<Entry<E, E>> iterator() {
                        return new Iterator<>() {

                            private final Iterator<E> iterator = set.iterator();

                            @Override
                            public boolean hasNext() {
                                return iterator.hasNext();
                            }

                            @Override
                            public Entry<E, E> next() {
                                if(!hasNext()) {
                                    throw new NoSuchElementException();
                                }
                                var elem = iterator.next();
                                return new AbstractMap.SimpleImmutableEntry<>(elem, elem);
                            }
                        };
                    }

                    @Override
                    public int size() {
                        return set.size();
                    }

                };
            }

            @Override
            public E get(Object key) {
                Objects.requireNonNull(key);
                return super.get(key);
            }

        };
    }

}

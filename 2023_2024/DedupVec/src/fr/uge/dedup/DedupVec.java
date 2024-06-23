package fr.uge.dedup;

import java.util.*;

public final class DedupVec<T> extends AbstractList<T> implements RandomAccess {

    private final List<T> elements;

    private final Map<T, T> references;

    public DedupVec() {
        this.elements = new ArrayList<>();
        this.references = new HashMap<>();
    }

    private DedupVec(Map<? extends T, ? extends T> map) {
        Objects.requireNonNull(map);
        this.elements = new ArrayList<>(map.values());
        this.references = new HashMap<>(map);
    }

    private static void checkPosition(int position, int size){
        Objects.checkIndex(position, size + 1);
    }

    public boolean add(T element){
        Objects.requireNonNull(element);
        var existingElem = references.get(element);
        if(existingElem == null){
            elements.add(element);
            references.put(element, element);
        } else {
            elements.add(existingElem);
        }
        return true;
    }

    @Override
    public void add(int index, T element) {
        Objects.requireNonNull(element);
        checkPosition(index, elements.size());
        var existingElem = references.get(element);
        if(existingElem == null){
            elements.add(index, element);
            references.put(element, element);
        } else {
            elements.add(index, existingElem);
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

    private boolean addAll(DedupVec<? extends T> other){
        Objects.requireNonNull(other);
        boolean flag = false;
        for(var key : other.references.keySet()){
            flag |= references.putIfAbsent(key, key) != null;
        }
        if(!flag) elements.addAll(other.elements);
        else other.elements.forEach(e -> elements.add(references.get(e)));
        return true;
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

    public static <E> DedupVec<E> fromSet(Set<? extends E> set) {
        return new DedupVec<>(newMapFromSet(set));
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        if(c instanceof DedupVec<? extends T> other) return addAll(other);
        return super.addAll(c);
    }

}

package fr.uge.cactuslist;

import java.util.AbstractList;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.Spliterator;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class CactusList<T> extends AbstractList<T> implements RandomAccess, Iterable<T> {

	private ArrayList<Object> cactusList = new ArrayList<>();

	private boolean isFrozen;

	private boolean isNormalized;

	private int size;

	public CactusList() {
		isNormalized = true;
	}

	public int size() {
		return size;
	}

	public boolean frozen() {
		return isFrozen;
	}

	public boolean normalized() {
		return isNormalized;
	}

	public boolean add(T element) {
		Objects.requireNonNull(element);
		if (isFrozen) throw new IllegalStateException();
		switch (element) {
			case CactusList<?> l -> throw new IllegalArgumentException();
			default -> cactusList.add(element);
		}
		size++;
		return true;
	}

	public void addCactus(CactusList<? extends T> other) {
		Objects.requireNonNull(other);
		if (isFrozen || other == this) {
			throw new IllegalStateException();
		}
		other.isFrozen = true;
		cactusList.add(other);
		size += other.size();
		isNormalized = false;
	}

	@SuppressWarnings("unchecked")
	public void forEach(Consumer<? super T> func) {
		Objects.requireNonNull(func);
		cactusList.forEach(e -> {
			switch (e) {
				case CactusList<?> l -> ((CactusList<T>) l).forEach(func);
				default -> func.accept((T) e);
			}
		});

	}

	@Override
	public String toString() {
		var sj = new StringJoiner(", ", "<", ">");
		this.forEach(e -> sj.add(e.toString()));
		return sj.toString();
	}

	public static <E> CactusList<E> from(List<E> lst) {
		Objects.requireNonNull(lst);
		var tmp = new CactusList<E>();
        tmp.addAll(lst);
		tmp.isFrozen = true;
		return tmp;
	}

	private void normalize() {
        cactusList = new ArrayList<>(this);
		isNormalized = true;
	}

	@SuppressWarnings("unchecked")
	public T get(int index) {
		Objects.checkIndex(index, size);
		if (!isNormalized) normalize();
		return (T) cactusList.get(index);
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<>() {

            private int currentIndex;
            private int cactusIndex;
            private final ArrayDeque<T> queue = new ArrayDeque<>();

            @Override
            public boolean hasNext() {
                return currentIndex < size;
            }

            @Override
            @SuppressWarnings("unchecked")
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                while (queue.isEmpty()) {
                    switch (cactusList.get(cactusIndex)) {
                        case CactusList<?> l -> {
                            l.forEach(e -> queue.offer((T) e));
                            cactusIndex++;
                        }
                        case Object e -> {
                            queue.offer((T) e);
                            cactusIndex++;
                        }
                    }
                }
                currentIndex++;
                return queue.poll();
            }
        };
	}
	
	@Override
	public Spliterator<T> spliterator() {
		return new Spliterator<>() {

            private int currentSize = size;
            private final Iterator<T> it = iterator();

            @Override
            public boolean tryAdvance(Consumer<? super T> action) {
                if (it.hasNext()) {
                    action.accept(it.next());
                    currentSize--;
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
                return currentSize;
            }

            @Override
            public int characteristics() {
                return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.NONNULL;
            }
        };
		
	}
	
	@Override
	public Stream<T> stream(){
		return StreamSupport.stream(spliterator(), false);
	}

}

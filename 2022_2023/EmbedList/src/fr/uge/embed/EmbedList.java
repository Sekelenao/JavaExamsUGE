package fr.uge.embed;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class EmbedList<T> extends AbstractList<T> implements Iterable<T> {

	public interface Entry<T extends Entry<T>> {
		T getNext();
		void setNext(T value);
	}

	private T head;

	private T tail;

	private int size;

	private final UnaryOperator<T> getNext;

	private final BiConsumer<T, T> setNext;

	private final boolean isUnmodifiable;

	public EmbedList(UnaryOperator<T> getNext, BiConsumer<T, T> setNext) {
		this(getNext, setNext, null, null, 0, false);
	}

	private EmbedList(UnaryOperator<T> getNext, BiConsumer<T, T> setNext, T head, T tail, int size, boolean isUnmodifiable) {
		if(size < 0) throw new IllegalArgumentException("Size cannot be negative");
		this.getNext = Objects.requireNonNull(getNext);
		this.setNext = Objects.requireNonNull(setNext);
		this.isUnmodifiable = isUnmodifiable;
		this.head = head;
		this.tail = tail;
		this.size = size;
	}

	public int size() {
		return size;
	}

	@Override
	public void addFirst(T element) {
		Objects.requireNonNull(element);
		if (isUnmodifiable) throw new UnsupportedOperationException();
		if (head != null) {
			setNext.accept(element, head);
		} else {
			tail = element;
		}
		head = element;
		size++;
	}

	@Override
	public void forEach(Consumer<? super T> action) {
		Objects.requireNonNull(action);
		Stream.iterate(head, Objects::nonNull, getNext).limit(size).forEach(action);
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<>() {

            private int position = 0;
            private T current = head;

            @Override
            public boolean hasNext() {
                return position < size;
            }

            @Override
            public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                var tmp = current;
                current = getNext.apply(current);
                position++;
                return tmp;
            }

        };
	}

	public T get(int index) {
		if (isUnmodifiable && index >= size || index < 0) {
			throw new IndexOutOfBoundsException();
		}
		if (index == size - 1 && index > 0) {
			return tail;
		}
		int i = 0;
		for(var e : this){
			if(i++ == index) return e;
		}
		throw new IndexOutOfBoundsException();
	}

	@Override
	public int indexOf(Object o) {
		Objects.requireNonNull(o);
		var it = iterator();
		for (int i = 0; it.hasNext(); i++) {
			if(it.next().equals(o)) return i;
		}
		return -1;
	}

	@Override
	public int lastIndexOf(Object o) {
		Objects.requireNonNull(o);
		var it = iterator();
		int lastEncountered = -1;
		for (int i = 0; it.hasNext(); i++) {
			if(it.next().equals(o)) lastEncountered = i;
		}
		return lastEncountered;
	}

	public EmbedList<T> unmodifiable() {
		return isUnmodifiable ? this : new EmbedList<>(getNext, setNext, head, tail, size, true);
	}

	@Override
	public boolean add(T element) {
		Objects.requireNonNull(element);
		if (isUnmodifiable) throw new UnsupportedOperationException();
		if (tail == null) {
			head = element;
        } else {
			setNext.accept(tail, element);
        }
        tail = element;
        size++;
		return true;
	}

	private <E> Spliterator<E> mappingSpliterator(Function<? super T, E> func) {
		Objects.requireNonNull(func);
		return new Spliterator<>() {

            private final Iterator<T> it = iterator();
            private long remaining = size;

            @Override
            public boolean tryAdvance(Consumer<? super E> action) {
                if (it.hasNext()) {
                    action.accept(func.apply(it.next()));
                    remaining--;
                    return true;
                }
                return false;
            }

            @Override
            public Spliterator<E> trySplit() {
                return null;
            }

            @Override
            public long estimateSize() {
                return remaining;
            }

            @Override
            public int characteristics() {
                if (isUnmodifiable) {
                    return Spliterator.IMMUTABLE | Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.SIZED;
                }
                return Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.SIZED;
            }

        };
	}

	@Override
	public Spliterator<T> spliterator() {
		return mappingSpliterator(e -> e);
	}

	public <E> Stream<E> valueStream(Function<? super T, E> function) {
		Objects.requireNonNull(function);
		return StreamSupport.stream(mappingSpliterator(function), false);
	}

	public static <E extends Entry<E>> EmbedList<E> of(Class<E> type) {
		Objects.requireNonNull(type);
		return new EmbedList<>(Entry::getNext, Entry::setNext);
	}

}

package fr.uge.expando;

import java.lang.reflect.RecordComponent;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

final class ExpandoEntrySet extends AbstractSet<Map.Entry<String, Object>> {

    private final Map<String, RecordComponent> fields;

    private final Map<String, Object> attributes;

    private final Object type;

    private final int isOrdered;

    ExpandoEntrySet(Map<String, RecordComponent> fields, Map<String, Object> attributes, Object type, boolean isOrdered){
        this.fields = Objects.requireNonNull(fields);
        this.attributes = Objects.requireNonNull(attributes);
        this.type = Objects.requireNonNull(type);
        this.isOrdered = isOrdered ? Spliterator.ORDERED : 0;
    }

    @Override
    public Iterator<Map.Entry<String, Object>> iterator() {
        return new Iterator<>() {

            private final Iterator<Map.Entry<String, RecordComponent>> fieldsIterator =
                    fields.entrySet().iterator();

            private final Iterator<Map.Entry<String, Object>> attributesIterator =
                    attributes.entrySet().iterator();

            @Override
            public boolean hasNext() {
                return fieldsIterator.hasNext() || attributesIterator.hasNext();
            }

            @Override
            public Map.Entry<String, Object> next() {
                if (fieldsIterator.hasNext()) {
                    var entry = fieldsIterator.next();
                    return Map.entry(entry.getKey(),
                            ExpandoUtils.invoke(entry.getValue().getAccessor(), type));
                }
                if (attributesIterator.hasNext()) {
                    return attributesIterator.next();
                }
                throw new NoSuchElementException();
            }
        };
    }

    @Override
    public int size() {
        return fields.size() + attributes.size();
    }

    @Override
    public Spliterator<Map.Entry<String, Object>> spliterator() {
        final var characteristics = Spliterator.DISTINCT | Spliterator.IMMUTABLE |
                Spliterator.NONNULL | Spliterator.SIZED | isOrdered;
        return new Spliterator<>() {

            private long remaining = size();

            private final Iterator<Map.Entry<String, Object>> iterator = iterator();

            @Override
            public boolean tryAdvance(Consumer<? super Map.Entry<String, Object>> action) {
                if (iterator.hasNext()) {
                    action.accept(iterator.next());
                    remaining--;
                    return true;
                }
                return false;
            }

            @Override
            public Spliterator<Map.Entry<String, Object>> trySplit() {
                int batchSize = 256;
                if (remaining > batchSize) {
                    Set<Map.Entry<String, Object>> batch = HashSet.newHashSet(batchSize);
                    for (int i = 0; i < batchSize && iterator.hasNext(); i++) {
                        batch.add(iterator.next());
                    }
                    remaining -= batch.size();
                    return Spliterators.spliterator(batch, characteristics);
                }
                return null;
            }

            @Override
            public long estimateSize() {
                return remaining;
            }

            @Override
            public int characteristics() {
                return characteristics;
            }
        };
    }

    @Override
    public Stream<Map.Entry<String, Object>> stream() {
        return StreamSupport.stream(spliterator(), true);
    }

}

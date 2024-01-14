package fr.uge.expando;

import java.lang.reflect.RecordComponent;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface Expando {

    Map<String, Object> moreAttributes();

    default Map<String, Object> asMap() {
        var type = this;
        return new AbstractMap<>() {

            private final Map<String, RecordComponent> fields = ExpandoUtils.CACHE.get(type.getClass());

            @Override
            public Set<Entry<String, Object>> entrySet() {
                return new AbstractSet<>() {

                    @Override
                    public Iterator<Entry<String, Object>> iterator() {
                        return new Iterator<>() {

                            private final Iterator<Entry<String, RecordComponent>> fieldsIterator =
                                    fields.entrySet().iterator();

                            private final Iterator<Entry<String, Object>> attributesIterator =
                                    moreAttributes().entrySet().iterator();

                            @Override
                            public boolean hasNext() {
                                return fieldsIterator.hasNext() || attributesIterator.hasNext();
                            }

                            @Override
                            public Entry<String, Object> next() {
                                if(fieldsIterator.hasNext()){
                                    var entry = fieldsIterator.next();
                                    return Map.entry(entry.getKey(),
                                            ExpandoUtils.invoke(entry.getValue().getAccessor(), type));
                                }
                                if(attributesIterator.hasNext()){
                                    var entry = attributesIterator.next();
                                    return Map.entry(entry.getKey(), entry.getValue());
                                }
                                throw new NoSuchElementException();
                            }
                        };
                    }

                    @Override
                    public int size() {
                        return fields.size() + moreAttributes().size();
                    }

                    @Override
                    public Spliterator<Entry<String, Object>> spliterator() {
                        return new Spliterator<>() {

                            private long remaining = size();

                            private final Iterator<Entry<String, Object>> iterator = iterator();

                            @Override
                            public boolean tryAdvance(Consumer<? super Entry<String, Object>> action) {
                                if (iterator.hasNext()) {
                                    action.accept(iterator.next());
                                    remaining--;
                                    return true;
                                }
                                return false;
                            }

                            @Override
                            public Spliterator<Entry<String, Object>> trySplit() {
                                return null;
                            }

                            @Override
                            public long estimateSize() {
                                return remaining;
                            }

                            @Override
                            public int characteristics() {
                                return Spliterator.DISTINCT | Spliterator.IMMUTABLE |
                                        Spliterator.NONNULL | Spliterator.SIZED;
                            }
                        };
                    }

                    @Override
                    public Stream<Entry<String, Object>> stream() {
                        return StreamSupport.stream(spliterator(), false);
                    }
                };
            }

            @Override
            public boolean containsKey(Object key) {
                return fields.containsKey(key) || moreAttributes().containsKey(key);
            }

            @Override
            public Object get(Object key) {
                var recordComponent = fields.get(key);
                return recordComponent == null ? moreAttributes().get(key) :
                        ExpandoUtils.invoke(recordComponent.getAccessor(), type);
            }

            @Override
            public Object getOrDefault(Object key, Object defaultValue) {
                var tmp = get(Objects.requireNonNull(key));
                return tmp == null ? defaultValue : tmp;
            }

            @Override
            public void forEach(BiConsumer<? super String, ? super Object> action) {
                Objects.requireNonNull(action);
                for(var e : entrySet()) action.accept(e.getKey(), e.getValue());
            }

        };
    }

}
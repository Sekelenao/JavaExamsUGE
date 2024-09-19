package fr.uge.expando;

import java.lang.reflect.RecordComponent;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface Expando {

    Map<String, Object> moreAttributes();

    default Map<String, Object> asMap() {
        final var instance = this;
        return new AbstractMap<>() {

            private final Map<String, RecordComponent> components = ExpandoUtils.RECORD_COMPONENTS_CACHE
                    .get(instance.getClass());

            private final int size = moreAttributes().size() + components.size();

            @Override
            public Set<Entry<String, Object>> entrySet() {
                return new AbstractSet<>() {

                    @Override
                    public Iterator<Entry<String, Object>> iterator() {
                        return new Iterator<>() {

                            private final Iterator<Entry<String, RecordComponent>> componentsIt = components.entrySet()
                                    .iterator();

                            private final Iterator<Map.Entry<String, Object>> moreAttrIt = moreAttributes().entrySet()
                                    .iterator();

                            @Override
                            public boolean hasNext() {
                                return componentsIt.hasNext() || moreAttrIt.hasNext();
                            }

                            @Override
                            public Entry<String, Object> next() {
                                if (componentsIt.hasNext()) {
                                    var entry = componentsIt.next();
                                    var value = ExpandoUtils.invoke(entry.getValue().getAccessor(), instance);
                                    return Map.entry(entry.getKey(), value);
                                }
                                if (moreAttrIt.hasNext()) return moreAttrIt.next();
                                throw new NoSuchElementException();
                            }
                        };
                    }

                    @Override
                    public int size() {
                        return size;
                    }

                    @Override
                    public Spliterator<Entry<String, Object>> spliterator() {
                        return new Spliterator<>() {

                            private final Iterator<Entry<String, RecordComponent>> componentsIt = components.entrySet()
                                    .iterator();

                            private final int componentsSize = components.size();

                            private int componentsIndex = 0;

                            private final Iterator<Map.Entry<String, Object>> moreAttrIt = moreAttributes().entrySet()
                                    .iterator();

                            private final int attributesSize = moreAttributes().size();

                            private int attributesIndex = 0;

                            private boolean isSplit = false;

                            @Override
                            public boolean tryAdvance(Consumer<? super Entry<String, Object>> action) {
                                if(componentsIt.hasNext()) {
                                    var entry = componentsIt.next();
                                    componentsIndex++;
                                    var value = ExpandoUtils.invoke(entry.getValue().getAccessor(), instance);
                                    action.accept(Map.entry(entry.getKey(), value));
                                    return true;
                                }
                                if(!isSplit && moreAttrIt.hasNext()) {
                                    action.accept(moreAttrIt.next());
                                    attributesIndex++;
                                    return true;
                                }
                                return false;
                            }

                            @Override
                            public Spliterator<Entry<String, Object>> trySplit() {
                                if(isSplit) return null;
                                isSplit = true;
                                return moreAttributes().entrySet().spliterator();
                            }

                            @Override
                            public long estimateSize() {
                                if (isSplit) return componentsSize - componentsIndex;
                                return (componentsSize + attributesSize) - (attributesIndex + componentsIndex);
                            }

                            @Override
                            public int characteristics() {
                                return SIZED | SUBSIZED | IMMUTABLE | DISTINCT | NONNULL | ORDERED;
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
                return components.containsKey(key) || moreAttributes().containsKey(key);
            }

            @Override
            public Object getOrDefault(Object key, Object defaultValue) {
                Objects.requireNonNull(key);
                var component = components.get(key);
                if (component != null) {
                    return ExpandoUtils.invoke(component.getAccessor(), instance);
                }
                return moreAttributes().getOrDefault(key, defaultValue);
            }

            @Override
            public Object get(Object key) {
                Objects.requireNonNull(key);
                return getOrDefault(key, null);
            }

            @Override
            public void forEach(BiConsumer<? super String, ? super Object> action) {
                Objects.requireNonNull(action);
                for(var entry : entrySet()) {
                    action.accept(entry.getKey(), entry.getValue());
                }
            }
        };
    }

}
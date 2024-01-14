package fr.uge.expando;

import java.lang.reflect.RecordComponent;
import java.util.*;

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
        };
    }

}
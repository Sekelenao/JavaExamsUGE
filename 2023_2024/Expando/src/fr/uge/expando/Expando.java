package fr.uge.expando;

import java.lang.reflect.RecordComponent;
import java.util.*;
import java.util.function.BiConsumer;

public interface Expando {

    Map<String, Object> moreAttributes();

    default Map<String, Object> asMap() {
        final var type = this;
        return new AbstractMap<>() {

            private final Map<String, RecordComponent> fields = ExpandoUtils.CACHE.get(type.getClass());

            @Override
            public Set<Entry<String, Object>> entrySet() {
                return new ExpandoEntrySet(fields, moreAttributes(), type);
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
                return containsKey(key) ? get(key) : defaultValue;
            }

            @Override
            public void forEach(BiConsumer<? super String, ? super Object> action) {
                Objects.requireNonNull(action);
                for (var e : entrySet()) action.accept(e.getKey(), e.getValue());
            }

        };
    }

}
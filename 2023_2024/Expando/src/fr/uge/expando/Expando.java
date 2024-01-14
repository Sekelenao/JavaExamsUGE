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

                // We transformed the map to an ImmutableCollection due to copyAttributes from ExpandoUtils.
                // So we can not get the original type.
                // TODO

                return switch (moreAttributes()){
                    case SequencedMap<String, Object> attr -> new ExpandoEntrySet(fields, attr, type, true);
                    default -> new ExpandoEntrySet(fields, moreAttributes(), type, false);
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
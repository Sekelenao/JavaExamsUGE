package fr.uge.orderedmap;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;

public final class OrderedMap<K, V> {

    private final Map.Entry<? extends K, ? extends V>[] entries;

    private OrderedMap(Map.Entry<? extends K, ? extends V>[] entries) {
        this.entries = entries;
        super();
    }

    @SuppressWarnings("unchecked")
    public static <K, V> OrderedMap<K, V> of(Map<? extends K, ? extends V> map) {
        Objects.requireNonNull(map);
        var nextEmptyIndex = 0;
        var array = (Map.Entry<? extends K, ? extends V>[]) new Map.Entry<?, ?>[map.size()];
        for (var entry : map.entrySet()) {
            var key = Objects.requireNonNull(entry.getKey());
            var value = entry.getValue();
            array[nextEmptyIndex++] = new AbstractMap.SimpleImmutableEntry<>(key, value);
        }
        return new OrderedMap<>(array);
    }

    public int size() {
        return entries.length;
    }

}

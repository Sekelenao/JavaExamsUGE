package fr.uge.expando;

import java.util.Map;
import java.util.stream.Collectors;

public interface Expando {

    Map<String, Object> moreAttributes();

    default Map<String, Object> asMap() {
        var map = ExpandoUtils.CACHE.get(this.getClass()).entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> ExpandoUtils.invoke(e.getValue().getAccessor(), this)));
        map.putAll(moreAttributes());
        return Map.copyOf(map);
    }

}
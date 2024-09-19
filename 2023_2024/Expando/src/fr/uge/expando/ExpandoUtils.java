package fr.uge.expando;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.*;
import java.util.stream.Collectors;

public final class ExpandoUtils {

    private static final String MORE_ATTR_NAME = "moreAttributes";

    private ExpandoUtils() {
        throw new AssertionError("You cannot instantiate this class.");
    }

    static final ClassValue<Map<String, RecordComponent>> RECORD_COMPONENTS_CACHE = new ClassValue<>() {

        @Override
        protected Map<String, RecordComponent> computeValue(Class<?> type) {
            return Arrays.stream(type.getRecordComponents())
                    .filter(recordComponent -> !recordComponent.getName().equals(MORE_ATTR_NAME))
                    .collect(
                            Collectors.toMap(
                                RecordComponent::getName,
                                recordComponent -> recordComponent,
                                (_, _) -> {throw new AssertionError();},
                                LinkedHashMap::new
                            )
                    );
        }

    };

    public static <R extends Record & Expando> Map<String, Object> copyAttributes(Map<String, Object> moreAttributes, Class<R> type){
        Objects.requireNonNull(moreAttributes);
        Objects.requireNonNull(type);
        var components = RECORD_COMPONENTS_CACHE.get(type);
        for(var entry : moreAttributes.entrySet()) {
            Objects.requireNonNull(entry.getValue());
            if(components.containsKey(entry.getKey())) {
                throw new IllegalArgumentException("You cannot add attributes with the same name of a record component");
            }
        }
        if(moreAttributes instanceof SequencedMap<String, Object>) {
            return Collections.unmodifiableMap(new LinkedHashMap<>(moreAttributes));
        }
        return Map.copyOf(moreAttributes);
    }

    static Object invoke(Method accessor, Object expando) {
        Objects.requireNonNull(accessor);
        Objects.requireNonNull(expando);
        try{
            return accessor.invoke(expando);
        }catch (IllegalAccessException e){
            throw new IllegalAccessError();
        } catch(InvocationTargetException e) {
            var cause = e.getCause();
            if (cause instanceof RuntimeException uncheckedException) { // Exceptions unchecked
                throw uncheckedException;
            }
            if (cause instanceof Error error) { // Error VM (critical)
                throw error;
            }
            throw new AssertionError(cause); // Other
        }
    }

}

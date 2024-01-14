package fr.uge.expando;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.*;
import java.util.stream.Collectors;

public final class ExpandoUtils {

    static final ClassValue<Map<String, RecordComponent>> CACHE = new ClassValue<>() {
        @Override
        protected Map<String, RecordComponent> computeValue(Class<?> type) {
            Objects.requireNonNull(type);
            return Arrays.stream(type.getRecordComponents())
                    .filter(r -> !r.getName().equals("moreAttributes"))
                    .collect(Collectors.toMap(RecordComponent::getName, r -> r,
                            (existingValueWithKey, valueSameKey) -> {throw new AssertionError();}, LinkedHashMap::new));
            //LinkedHashMap for Q9
        }
    };

    private ExpandoUtils(){
        throw new AssertionError("You cannot instantiate this class.");
    }

    public static <R extends Record & Expando> Map<String, Object> copyAttributes(Map<String, Object> moreAttributes, Class<R> type){
        Objects.requireNonNull(type);
        Objects.requireNonNull(moreAttributes);
        for(var e : moreAttributes.entrySet()){
            Objects.requireNonNull(e.getValue());
            if(CACHE.get(type).containsKey(Objects.requireNonNull(e.getKey()))){
                throw new IllegalArgumentException("You cannot add attributes with the same name of a record component");
            }
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

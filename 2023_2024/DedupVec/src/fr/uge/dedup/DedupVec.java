package fr.uge.dedup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public final class DedupVec<T> {

    private final ArrayList<T> elements = new ArrayList<>();

    private final HashMap<T, T> references = new HashMap<>();

    public void add(T element){
        Objects.requireNonNull(element);
        var existingElem = references.get(element);
        if(existingElem == null){
            elements.add(element);
            references.put(element, element);
        } else {
            elements.add(existingElem);
        }
    }

    public T get(int index){
        return elements.get(index);
    }

    public int size(){
        return elements.size();
    }

}

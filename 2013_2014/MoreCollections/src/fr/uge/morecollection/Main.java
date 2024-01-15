package fr.uge.morecollection;

import java.util.List;

public final class Main {

    public static void main(String[] args) {
        var test = List.of(1, 2, 4, 8);
        System.out.println(MoreCollections.asMappedList(test, i -> i * 2));
        System.out.println(MoreCollections.asMappedList(test, Object::toString));
    }

}

package fr.uge.configurations;

import static java.util.Map.entry;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@SuppressWarnings("static-method")
public class ConfTest {

    @Test
    @Tag("Q1")
    public void logLevels() {
        assertAll(
                () -> assertEquals("INFO", LogLevel.INFO.name()),
                () -> assertEquals("ERROR", LogLevel.ERROR.name()),
                () -> assertEquals("WARNING", LogLevel.WARNING.name())
        );
    }

    @Test
    @Tag("Q1")
    public void logLevelsOrder() {
        assertAll(
                () -> assertTrue(LogLevel.INFO.compareTo(LogLevel.WARNING) < 0),
                () -> assertTrue(LogLevel.WARNING.compareTo(LogLevel.ERROR) < 0),
                () -> assertTrue(LogLevel.INFO.compareTo(LogLevel.ERROR) < 0),
                () -> assertEquals(0, LogLevel.INFO.ordinal()),
                () -> assertEquals(1, LogLevel.WARNING.ordinal()),
                () -> assertEquals(2, LogLevel.ERROR.ordinal())
        );
    }

    @Test
    @Tag("Q2")
    public void loggerConfEmptyByDefault() {
        assertAll(
                () -> assertTrue(new LoggerConf().name().isEmpty()),
                () -> assertTrue(new LoggerConf().level().isEmpty())
        );
    }

    @Test
    @Tag("Q2")
    public void loggerConfSetters() {
        assertAll(() -> {
                    var conf = new LoggerConf();
                    conf.name("bob");
                    assertEquals("bob", conf.name().orElseThrow());
                }, () -> {
                    var conf = new LoggerConf();
                    conf.level(LogLevel.WARNING);
                    assertEquals(LogLevel.WARNING, conf.level().orElseThrow());
                }, () -> {
                    var conf = new LoggerConf();
                    conf.name("lisa");
                    conf.level(LogLevel.ERROR);
                    assertEquals("lisa", conf.name().orElseThrow());
                    assertEquals(LogLevel.ERROR, conf.level().orElseThrow());
                }
        );
    }

    @Test
    @Tag("Q2")
    public void loggerConfChainedSetters() {
        assertAll(
                () -> assertEquals("Test", new LoggerConf().name("Test").name().orElseThrow()),
                () -> assertEquals(LogLevel.INFO, new LoggerConf().level(LogLevel.INFO).level().orElseThrow())
        );
    }

    @Test
    @Tag("Q2")
    public void loggerConfSettersNPE() {
        var conf = new LoggerConf();
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> conf.name(null)),
                () -> assertThrows(NullPointerException.class, () -> conf.level(null))
        );
    }

    @Test
    @Tag("Q2")
    public void loggerConfNoOptionalField() {
        assertTrue(Arrays.stream(LoggerConf.class.getDeclaredFields()).allMatch(not(f -> f.getType() == Optional.class)));
    }

    @Test
    @Tag("Q3")
    public void loggerConfToString() {
        assertAll(
                () -> assertEquals("{}", new LoggerConf().toString()),
                () -> assertEquals("{jack}", new LoggerConf().name("jack").toString()),
                () -> assertEquals("{ERROR}", new LoggerConf().level(LogLevel.ERROR).toString()),
                () -> assertEquals("{jane, INFO}", new LoggerConf().name("jane").level(LogLevel.INFO).toString()),
                () -> assertEquals("{jane, INFO}", new LoggerConf().level(LogLevel.INFO).name("jane").toString())
        );
    }

    @Test
    @Tag("Q4")
    public void confHelperToString() {
        var conf = new LoggerConf().name("hello").level(LogLevel.WARNING);
        assertAll(
                () -> assertEquals("{}", ConfHelper.toString(conf)),
                () -> assertEquals("{hello}", ConfHelper.toString(conf, LoggerConf::name)),
                () -> assertEquals("{WARNING}", ConfHelper.toString(conf, LoggerConf::level)),
                () -> assertEquals("{hello, WARNING}", ConfHelper.toString(conf, LoggerConf::name, LoggerConf::level)),
                () -> assertEquals("{WARNING, hello}", ConfHelper.toString(conf, LoggerConf::level, LoggerConf::name))
        );
    }

    @Test
    @Tag("Q4")
    public void confHelperToStringThrows() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> ConfHelper.toString(null)),
                () -> assertThrows(NullPointerException.class, () -> ConfHelper.toString(new LoggerConf(), null)),
                () -> assertThrows(NullPointerException.class, () -> ConfHelper.toString(new LoggerConf(), LoggerConf::name, null))
        );
    }

    @Test
    @Tag("Q5")
    public void confHelperGenericToString() {
        var s = "foo";
        assertAll(
                () -> assertEquals("{}", ConfHelper.toString(s)),
                () -> assertEquals("{foo}", ConfHelper.toString(s, Optional::of))
        );
    }

    @Test
    @Tag("Q5")
    public void confHelperGenericToString2() {
        var stream = Stream.of(3);
        assertAll(
                () -> assertEquals("{}", ConfHelper.toString(stream)),
                () -> assertEquals("{3}", ConfHelper.toString(stream, Stream::findFirst))
        );
    }

    @Test
    @Tag("Q5")
    public void confHelperGenericToString3() {
        var s = "bar";
        assertAll(
                () -> assertEquals("{}", ConfHelper.toString(s)),
                () -> assertEquals("{bar}", ConfHelper.toString(s, (Object o) -> Optional.of(o)))
        );
    }

    @Test
    @Tag("Q5")
    public void confHelperGenericToString4() {
        var s = "baz";
        assertEquals("{baz, baz}", ConfHelper.toString(s, (Object o) -> Optional.of(o), (Object o) -> Optional.of(o.toString())));
    }

    @Test
    @Tag("Q6")
    public void confHelperToExtendedString() {
        var conf = new LoggerConf().name("hello").level(LogLevel.WARNING);
        assertAll(
                () -> assertEquals("{}", ConfHelper.toExtendedString(conf)),
                () -> assertEquals("{name: hello}", ConfHelper.toExtendedString(conf, entry("name", LoggerConf::name))),
                () -> assertEquals("{level: WARNING}", ConfHelper.toExtendedString(conf, entry("level", LoggerConf::level))),
                () -> assertEquals("{name: hello, level: WARNING}", ConfHelper.toExtendedString(conf, entry("name", LoggerConf::name), entry("level", LoggerConf::level))),
                () -> assertEquals("{level: WARNING, name: hello}", ConfHelper.toExtendedString(conf, entry("level", LoggerConf::level), entry("name", LoggerConf::name)))
        );
    }

    @Test
    @Tag("Q6")
    public void confHelperToExtendedString2() {
        var stream = Stream.of(3);
        assertAll(
                () -> assertEquals("{}", ConfHelper.toExtendedString(stream)),
                () -> assertEquals("{first: 3}", ConfHelper.toExtendedString(stream, entry("first", Stream::findFirst)))
        );
    }

    @Test
    @Tag("Q6")
    public void confHelperToExtendedString3() {
        var s = "foo";
        assertAll(
                () -> assertEquals("{}", ConfHelper.toExtendedString(s)),
                () -> assertEquals("{key: foo}", ConfHelper.toExtendedString(s, entry("key", Optional::of)))
        );
    }

    @Test
    @Tag("Q6")
    public void confHelperToExtendedString4() {
        var s = "bar";
        assertAll(
                () -> assertEquals("{}", ConfHelper.toExtendedString(s)),
                () -> assertEquals("{key: bar}", ConfHelper.toExtendedString(s, entry("key", (Object o) -> Optional.of(o))))
        );
    }

    @Test
    @Tag("Q6")
    public void confHelperToExtendedString5() {
        var s = "baz";
        assertEquals("{key: baz, key2: baz}", ConfHelper.toExtendedString(s,
                entry("key", (Object o) -> Optional.of(o)),
                entry("key2", (Object o) -> Optional.of(o.toString()))));
    }

    @Test
    @Tag("Q6")
    public void confHelperToExtendedStringThrows() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> ConfHelper.toExtendedString(null)),
                () -> assertThrows(NullPointerException.class, () -> ConfHelper.toExtendedString(new LoggerConf(), null)),
                () -> assertThrows(NullPointerException.class, () -> ConfHelper.toExtendedString(new LoggerConf(), entry("name", LoggerConf::name), null))
        );
    }

    @Test
    @Tag("Q7")
    public void confHelperGenerateAll() {
        var set = ConfHelper.generateAll(LoggerConf::new, c -> c.name("jay"), c -> c.level(LogLevel.WARNING));
        assertEquals(Set.of("{}", "{WARNING}", "{jay}", "{jay, WARNING}"), set.stream().map(LoggerConf::toString).collect(toSet()));
    }

    @Test
    @Tag("Q7")
    public void confHelperGenerateAll2() {
        var set = ConfHelper.generateAll(StringBuilder::new, b -> b.append("foo"), b -> b.append("bar"));
        assertEquals(Set.of("", "bar", "foo", "foobar"), set.stream().map(StringBuilder::toString).collect(toSet()));
    }

    @Test
    @Tag("Q7")
    public void confHelperGenerateAll3() {
        var set = ConfHelper.generateAll(ArrayList::new, l -> {
            l.add("foo");
            return l;
        }, l -> {
            l.add("bar");
            return l;
        });
        assertEquals(Set.of(List.of(), List.of("bar"), List.of("foo"), List.of("foo", "bar")), set);
    }

    @Test
    @Tag("Q7")
    public void confHelperGenerateAllThrows() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> ConfHelper.generateAll(null, x -> x)),
                () -> assertThrows(NullPointerException.class, () -> ConfHelper.generateAll(LoggerConf::new, null)),
                () -> assertThrows(NullPointerException.class, () -> ConfHelper.generateAll(LoggerConf::new, null, x -> x))
        );
    }

    @Test
    @Tag("Q8")
    public void confHelperGenerateAllAsStream() {
        var stream = ConfHelper.generateAllAsStream(LoggerConf::new, c -> c.name("jay"), c -> c.level(LogLevel.WARNING));
        assertEquals(Set.of("{}", "{WARNING}", "{jay}", "{jay, WARNING}"), stream.map(LoggerConf::toString).collect(toSet()));
    }

    @Test
    @Tag("Q8")
    public void confHelperGenerateAllAsStream2() {
        var stream = ConfHelper.generateAllAsStream(StringBuilder::new, b -> b.append("foo"), b -> b.append("bar"));
        assertEquals(Set.of("", "bar", "foo", "foobar"), stream.map(StringBuilder::toString).collect(toSet()));
    }

    @Test
    @Tag("Q8")
    public void confHelperGenerateAllAsStream3() {
        var stream = ConfHelper.generateAllAsStream(ArrayList<String>::new, l -> {
            l.add("foo");
            return l;
        }, l -> {
            l.add("bar");
            return l;
        });
        assertEquals(Set.of(List.of(), List.of("bar"), List.of("foo"), List.of("foo", "bar")), stream.collect(toSet()));
    }

    @Test
    @Tag("Q8")
    public void confHelperGenerateAllAsStreamThrows() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> ConfHelper.generateAllAsStream(null, x -> x)),
                () -> assertThrows(NullPointerException.class, () -> ConfHelper.generateAllAsStream(LoggerConf::new, null)),
                () -> assertThrows(NullPointerException.class, () -> ConfHelper.generateAllAsStream(LoggerConf::new, null, x -> x))
        );
    }

}

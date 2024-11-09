package year2023.day5;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

enum Category {
    SEED, SOIL, FERTILIZER, WATER, LIGHT, TEMPERATURE, HUMIDITY, LOCATION;

    static Category from(String name) {
        return Arrays.stream(values()).filter(c -> c.name().toLowerCase().equals(name)).findFirst().orElseThrow();
    }
}

record Range(long start, long length) implements Comparable<Range> {
    Range {
        if (length <= 0) {
            throw new IllegalArgumentException();
        }
    }

    static Range from(long l) {
        return new Range(l, 1);
    }

    static Range fromTo(long from, long to) {
        return new Range(from, to - from);
    }

    long end() {
        return start + length;
    }

    boolean intersects(Range other) {
        return start() < other.end() && other.start() < end();
    }

    Range intersection(Range other) {
        long left = Long.max(start(), other.start());
        long right = Long.min(end(), other.end());
        return new Range(left, right - left);
    }

    @Override
    public int compareTo(Range o) {
        return Long.compare(start, o.start);
    }
}

record MapEntry(long destStart, Range range) implements Comparable<MapEntry> {
    private static final Pattern DIGITS = Pattern.compile("\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s*");

    static MapEntry from(String s) {
        Matcher matcher = DIGITS.matcher(s);
        if (!matcher.matches()) {
            throw new IllegalArgumentException();
        }
        return new MapEntry(Long.parseLong(matcher.group(1)), new Range(Long.parseLong(matcher.group(2)), Long.parseLong(matcher.group(3))));
    }

    Optional<MapEntry> matches(long l) {
        return Range.from(l).intersects(range) ? Optional.of(this) : Optional.empty();
    }

    long remap(long l) {
        return l - range.start() + destStart;
    }

    Range remap(Range other) {
        return new Range(other.start() - range.start() + destStart, other.length());
    }

    @Override
    public int compareTo(MapEntry o) {
        return range.compareTo(o.range);
    }
}

record TMap(Category from, Category to, SortedSet<MapEntry> entries) {
    TMap(String fromName, String toName) {
        this(Category.from(fromName), Category.from(toName), new TreeSet<>());
    }

    long map(long l) {
        return entries.stream().map(e -> e.matches(l)).filter(Optional::isPresent).map(Optional::get).findFirst().map(e -> e.remap(l)).orElse(l);
    }

    SortedSet<Range> map(Set<Range> in) {
        SortedSet<Range> out = new TreeSet<>();
        in.forEach(range -> entries.stream().filter(e -> e.range().intersects(range)).forEach(e -> out.add(e.remap(e.range().intersection(range)))));
        return out;
    }

    void fillHoles() {
        Set<MapEntry> holes = new TreeSet<>();
        Iterator<MapEntry> iterator = entries.iterator();
        MapEntry prev = null;
        while (iterator.hasNext()) {
            MapEntry next = iterator.next();
            if (prev != null) {
                if (prev.range().start() >= next.range().start()) {
                    throw new IllegalStateException();
                }
                if (prev.range().intersects(next.range())) {
                    throw new IllegalStateException();
                }
                if (prev.range().end() < next.range().start()) {
                    holes.add(new MapEntry(prev.range().end(), Range.fromTo(prev.range().end(), next.range().start())));
                }
            }
            prev = next;
        }
        entries.addAll(holes);
        entries.add(new MapEntry(-1, Range.fromTo(-1, entries.first().range().start())));
        entries.add(new MapEntry(entries.last().range().end(), Range.fromTo(entries.last().range().end(), Long.MAX_VALUE)));
    }
}

public class Day5 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve(LocationFinder::solve);
    }
}

class LocationFinder {
    private static final Pattern HEADER = Pattern.compile("seeds: (.*)");
    private static final Pattern DIGIT = Pattern.compile("(\\d+) +(\\d+)");
    private static final Pattern MAP = Pattern.compile("(.*)-to-(.*) map:");
    private final Set<Long> seeds = new HashSet<>();
    private final SortedSet<Range> ranges = new TreeSet<>();
    private final Map<Category, TMap> maps = new TreeMap<>();
    TMap tMap;
    State state = State.INIT;

    long location(long seed) {
        final Map<Category, Long> values = new TreeMap<>();
        values.put(Category.SEED, seed);
        Arrays.stream(Category.values()).filter(c -> c != Category.LOCATION).forEachOrdered(c -> values.put(maps.get(c).to(), maps.get(c).map(values.get(maps.get(c).from()))));
        return values.get(Category.LOCATION);
    }

    long location2(SortedSet<Range> ranges) {
        final Map<Category, SortedSet<Range>> values = new TreeMap<>();
        values.put(Category.SEED, ranges);
        Arrays.stream(Category.values()).filter(c -> c != Category.LOCATION).forEachOrdered(c -> values.put(maps.get(c).to(), maps.get(c).map(values.get(maps.get(c).from()))));
        return values.get(Category.LOCATION).first().start();
    }

    void parse(String line) {
        switch (state) {
            case INIT -> parseHeader(line);
            case BLANK -> parseMapHeader(line);
            case DIGITS -> parseMapEntry(line);
        }
    }

    void parseHeader(String line) {
        Matcher matcher = HEADER.matcher(line);
        if (!matcher.matches()) {
            throw new IllegalArgumentException();
        }
        for (Matcher m = DIGIT.matcher(matcher.group(1)); m.find(); ) {
            long start = Long.parseLong(m.group(1));
            long length = Long.parseLong(m.group(2));
            seeds.add(start);
            seeds.add(length);
            ranges.add(new Range(start, length));
        }
        state = State.BLANK;
    }

    void parseMapHeader(String line) {
        if (line.isBlank()) {
            return;
        }
        Matcher matcher = MAP.matcher(line);
        if (!matcher.matches()) {
            throw new IllegalArgumentException();
        }
        tMap = new TMap(matcher.group(1), matcher.group(2));
        maps.put(tMap.from(), tMap);
        state = State.DIGITS;
    }

    void parseMapEntry(String line) {
        if (line.isBlank()) {
            state = State.BLANK;
            return;
        }
        tMap.entries().add(MapEntry.from(line));
    }

    void solve() {
        maps.values().forEach(TMap::fillHoles);
        System.out.println(seeds.stream().map(this::location).mapToLong(a -> a).min().orElseThrow());
        System.out.println(location2(ranges));
    }

    enum State {INIT, BLANK, DIGITS}
}

class Puzzle {
    void solve(Consumer<LocationFinder> solver) throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("/year2023/day5/day5_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            LocationFinder locationFinder = new LocationFinder();
            reader.lines().forEach(locationFinder::parse);
            solver.accept(locationFinder);
        }
    }
}

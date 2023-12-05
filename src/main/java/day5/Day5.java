package day5;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

enum Category {
    SEED, SOIL, FERTILIZER, WATER, LIGHT, TEMPERATURE, HUMIDITY, LOCATION;

    static Category from(String name) {
        return Arrays.stream(values()).filter(c -> c.name().toLowerCase().equals(name)).findFirst().orElseThrow();
    }
}

record MapEntry(long destStart, long sourceStart, long length) {
    private static final Pattern DIGITS = Pattern.compile("\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s*");

    static MapEntry from(String s) {
        Matcher matcher = DIGITS.matcher(s);
        if (!matcher.matches()) {
            throw new IllegalArgumentException();
        }
        return new MapEntry(Long.parseLong(matcher.group(1)), Long.parseLong(matcher.group(2)), Long.parseLong(matcher.group(3)));
    }

    Optional<MapEntry> matches(long l) {
        return l >= sourceStart && l < sourceStart + length ? Optional.of(this) : Optional.empty();
    }

    long remap(long l) {
        return l - sourceStart + destStart;
    }
}

record TMap(Category from, Category to, List<MapEntry> entries) {
    TMap(String fromName, String toName) {
        this(Category.from(fromName), Category.from(toName), new ArrayList<>());
    }

    long map(long l) {
        return entries.stream().map(e -> e.matches(l)).filter(Optional::isPresent).map(Optional::get).findFirst().map(e -> e.remap(l)).orElse(l);
    }
}

public class Day5 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve(LocationFinder::solve);
    }
}

class LocationFinder {
    private static final Pattern HEADER = Pattern.compile("seeds: (.*)");
    private static final Pattern DIGIT = Pattern.compile("(\\d+)");
    private static final Pattern MAP = Pattern.compile("(.*)-to-(.*) map:");
    private final Set<Long> seeds = new HashSet<>();
    private final Map<Category, TMap> maps = new TreeMap<>();
    TMap tMap;
    State state = State.INIT;

    long location(long seed) {
        final Map<Category, Long> values = new TreeMap<>();
        values.put(Category.SEED, seed);
        Arrays.stream(Category.values()).filter(c -> c != Category.LOCATION).forEachOrdered(c -> values.put(maps.get(c).to(), maps.get(c).map(values.get(maps.get(c).from()))));
        return values.get(Category.LOCATION);
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
            seeds.add(Long.parseLong(m.group(1)));
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
        System.out.println(seeds.stream().map(this::location).mapToLong(a -> a).min().orElseThrow());
    }

    enum State {INIT, BLANK, DIGITS}
}

class Puzzle {
    void solve(Consumer<LocationFinder> solver) throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("/day5/day5_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            LocationFinder locationFinder = new LocationFinder();
            reader.lines().forEach(locationFinder::parse);
            solver.accept(locationFinder);
        }
    }
}

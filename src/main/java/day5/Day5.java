package day5;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

record MapEntry(long destStart, long sourceStart, long length) {
    private static final Pattern DIGITS = Pattern.compile("\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s*");

    static MapEntry from(String s) {
        Matcher matcher = DIGITS.matcher(s);
        if (!matcher.matches()) {
            throw new IllegalArgumentException();
        }
        return new MapEntry(Long.parseLong(matcher.group(1)), Long.parseLong(matcher.group(2)), Long.parseLong(matcher.group(3)));
    }
}

record TMap(String name, List<MapEntry> entries) {
    TMap(String name) {
        this(name, new ArrayList<>());
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
    private static final Pattern MAP = Pattern.compile("(.*) map:");
    private final Set<Long> seeds = new HashSet<>();
    private final List<TMap> maps = new ArrayList<>();
    TMap tMap;
    State state = State.INIT;

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
        tMap = new TMap(matcher.group(1));
        maps.add(tMap);
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
        System.out.println(maps);
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

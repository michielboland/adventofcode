package year2023.day14;

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
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

enum Rock {
    ROUND('O'), CUBE('#'), SPACE('.');
    final char label;

    Rock(char label) {
        this.label = label;
    }

    static Rock from(int label) {
        return Arrays.stream(values()).filter(r -> r.label == label).findFirst().orElseThrow();
    }
}

public class Day14 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve();
    }
}

record Coordinate(int x, int y) implements Comparable<Coordinate> {
    Coordinate north() {
        return new Coordinate(x, y - 1);
    }

    Coordinate south() {
        return new Coordinate(x, y + 1);
    }

    Coordinate west() {
        return new Coordinate(x - 1, y);
    }

    Coordinate east() {
        return new Coordinate(x + 1, y);
    }

    boolean inside(int width, int height) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    @Override
    public int compareTo(Coordinate o) {
        return y == o.y ? Integer.compare(x, o.x) : Integer.compare(y, o.y);
    }
}

record Platform(Map<Coordinate, Rock> rocks, int width, int height) {
    static Platform parse(Stream<String> lines) {
        Map<Coordinate, Rock> rocks = new TreeMap<>();
        Set<Integer> widths = new TreeSet<>();
        var y = new AtomicInteger();
        lines.forEach(line -> {
            var x = new AtomicInteger();
            line.chars().forEach(c -> {
                var rock = Rock.from(c);
                if (rock != Rock.SPACE) {
                    rocks.put(new Coordinate(x.get(), y.get()), rock);
                }
                x.incrementAndGet();
            });
            widths.add(x.get());
            y.incrementAndGet();
        });
        if (widths.size() != 1) {
            throw new IllegalArgumentException();
        }
        var width = widths.stream().findFirst().orElseThrow();
        var height = y.get();
        return new Platform(rocks, width, height);
    }

    void tilt(Supplier<Coordinate> s, Function<Coordinate, Coordinate> f) {
        for (Coordinate a = s.get(), b = a; a.inside(width, height); a = f.apply(a)) {
            var rock = rocks.get(a);
            if (rock != null) {
                switch (rock) {
                    case ROUND -> {
                        if (rocks.get(b) == null) {
                            rocks.remove(a);
                            rocks.put(b, rock);
                        }
                        b = f.apply(b);
                    }
                    case CUBE -> b = f.apply(a);
                }
            }
        }
    }

    void tiltNorth() {
        IntStream.range(0, width).forEach(x -> tilt(() -> new Coordinate(x, 0), Coordinate::south));
    }

    void tiltWest() {
        IntStream.range(0, height).forEach(y -> tilt(() -> new Coordinate(0, y), Coordinate::east));
    }

    void tiltSouth() {
        IntStream.range(0, width).forEach(x -> tilt(() -> new Coordinate(x, height - 1), Coordinate::north));
    }

    void tiltEast() {
        IntStream.range(0, width).forEach(y -> tilt(() -> new Coordinate(width - 1, y), Coordinate::west));
    }

    void cycle() {
        tiltNorth();
        tiltWest();
        tiltSouth();
        tiltEast();
    }

    int load() {
        return rocks.entrySet().stream().filter(e -> e.getValue() == Rock.ROUND).mapToInt(e -> height - e.getKey().y()).sum();
    }

    Rock at(int x, int y) {
        return Optional.ofNullable(rocks.get(new Coordinate(x, y))).orElse(Rock.SPACE);
    }

    @Override
    public String toString() {
        return IntStream.range(0, height).mapToObj(y -> IntStream.range(0, width).mapToObj(x -> String.valueOf(at(x, y).label)).collect(Collectors.joining())).collect(Collectors.joining("\n"));
    }
}

class Puzzle {
    void solve() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("/year2023/day14/day14_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            var platform = Platform.parse(reader.lines());
            platform.tiltNorth();
            System.out.println(platform.load());
            List<String> l = new ArrayList<>();
            List<Integer> l2 = new ArrayList<>();
            do {
                platform.cycle();
                l.add(platform.toString());
                l2.add(platform.load());
            } while (l.size() == new HashSet<>(l).size());
            int last = l.size() - 1;
            int first = l.indexOf(l.get(last));
            System.out.println(l2.get(first + (1000000000 - 1 - first) % (last - first)));
        }
    }
}

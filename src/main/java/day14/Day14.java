package day14;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
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

    void tiltColumnNorth(int x) {
        for (Coordinate a = new Coordinate(x, 0), b = a; a.y() < height; a = a.south()) {
            var rock = rocks.get(a);
            if (rock != null) {
                switch (rock) {
                    case ROUND -> {
                        if (rocks.get(b) == null) {
                            rocks.remove(a);
                            rocks.put(b, rock);
                        }
                        b = b.south();
                    }
                    case CUBE -> b = a.south();
                }
            }
        }
    }

    void tiltNorth() {
        for (int x = 0; x < width; x++) {
            tiltColumnNorth(x);
        }
    }

    int load() {
        return rocks.entrySet().stream().filter(e -> e.getValue() == Rock.ROUND).mapToInt(e -> height - e.getKey().y()).sum();
    }
}

class Puzzle {
    void solve() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("/day14/day14_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            var platform = Platform.parse(reader.lines());
            platform.tiltNorth();
            System.out.println(platform.load());
        }
    }
}

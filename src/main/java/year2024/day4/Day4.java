package year2024.day4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

enum Direction {
    NORTH(Coordinate::north), NORTHEAST(c -> c.north().east()), EAST(Coordinate::east), SOUTHEAST(c -> c.south().east()),
    SOUTH(Coordinate::south), SOUTHWEST(c -> c.south().west()), WEST(Coordinate::west), NORTHWEST(c -> c.north().west());

    final Function<Coordinate, Coordinate> mover;

    Direction(Function<Coordinate, Coordinate> mover) {
        this.mover = mover;
    }

    Coordinate from(Coordinate c) {
        return mover.apply(c);
    }
}

public class Day4 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve();
    }
}

record Coordinate(int x, int y) {

    Coordinate east() {
        return new Coordinate(x + 1, y);
    }

    Coordinate west() {
        return new Coordinate(x - 1, y);
    }

    Coordinate north() {
        return new Coordinate(x, y - 1);
    }

    Coordinate south() {
        return new Coordinate(x, y + 1);
    }
}

class Puzzle {
    final Map<Character, Set<Coordinate>> map = new HashMap<>();
    AtomicInteger yCounter = new AtomicInteger();

    Puzzle() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("day4_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            reader.lines().forEachOrdered(this::addRow);
        }
    }

    void addRow(String s) {
        int y = yCounter.incrementAndGet();
        var row = new Row(map, y, new AtomicInteger());
        s.chars().forEachOrdered(i -> row.parse((char) i));
    }

    void solve() {
        int hits = 0;
        Set<Coordinate> startingCoordinates = map.get('X');
        for (var c : startingCoordinates) {
            for (var d : Direction.values()) {
                hits += hit(c, d, 'M');
            }
        }
        System.out.println(hits);
    }

    private int hit(Coordinate c, Direction d, char nextChar) {
        var nextCoordinate = d.from(c);
        if (!map.get(nextChar).contains(nextCoordinate)) {
            return 0;
        }
        return switch (nextChar) {
            case 'M' -> hit(nextCoordinate, d, 'A');
            case 'A' -> hit(nextCoordinate, d, 'S');
            case 'S' -> 1;
            default -> 0;
        };
    }

    record Row(Map<Character, Set<Coordinate>> map, int y, AtomicInteger xCounter) {
        void parse(char c) {
            int x = xCounter.incrementAndGet();
            var coordinate = new Coordinate(x, y);
            map.compute(c, (k, v) -> {
                if (v == null) {
                    v = new HashSet<>();
                }
                v.add(coordinate);
                return v;
            });
        }
    }
}

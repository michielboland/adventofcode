package year2022.day14;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Puzzle {
    final Set<Coordinate> walls = new HashSet<>();
    final int minX;
    final int maxX;
    final int maxY;

    Puzzle() throws Exception {
        try (var reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("day14_input"))))) {
            walls.addAll(reader.lines().flatMap(l -> Coordinate.wall(l).stream()).collect(Collectors.toSet()));
        }
        minX = walls.stream().mapToInt(Coordinate::x).min().orElseThrow();
        maxX = walls.stream().mapToInt(Coordinate::x).max().orElseThrow();
        maxY = walls.stream().mapToInt(Coordinate::y).max().orElseThrow();
    }

    public static void main(String[] args) throws Exception {
        new Puzzle().solve();
    }

    String toString(Set<Coordinate> sand) {
        var sb = new StringBuilder();
        for (int y = 0; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                var c = new Coordinate(x, y);
                sb.append(x == 500 && y == 0 ? '+' : sand.contains(c) ? 'o' : walls.contains(c) ? '#' : '.');
            }
            sb.append('\n');
        }
        sb.append('\n');
        return sb.toString();
    }

    void solve() {
        System.out.println(part1());
    }

    boolean drop(Set<Coordinate> sand) {
        var unit = new Coordinate(500, 0);
        while (unit.y() < maxY) {
            Coordinate next = null;
            for (var candidate : unit.next()) {
                if (!sand.contains(candidate) && !walls.contains(candidate)) {
                    next = candidate;
                    break;
                }
            }
            if (next == null) {
                sand.add(unit);
                return true;
            } else {
                unit = next;
            }
        }
        return false;
    }

    int part1() {
        int units = 0;
        Set<Coordinate> sand = new HashSet<>();
        System.out.println(toString(sand));
        while (drop(sand)) {
            ++units;
        }
        System.out.println(toString(sand));
        return units;
    }
}

record Coordinate(int x, int y) {
    @Override
    public String toString() {
        return x + "," + y;
    }

    List<Coordinate> next() {
        return List.of(new Coordinate(x, y + 1), new Coordinate(x - 1, y + 1), new Coordinate(x + 1, y + 1));
    }

    static Coordinate from(String input) {
        var parts = input.split(",");
        return new Coordinate(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }

    static Collection<Coordinate> wall(Coordinate from, Coordinate to) {
        if (from.x == to.x) {
            return IntStream.rangeClosed(Math.min(from.y, to.y), Math.max(from.y, to.y)).mapToObj(y -> new Coordinate(from.x, y)).collect(Collectors.toSet());
        } else if (from.y == to.y) {
            return IntStream.rangeClosed(Math.min(from.x, to.x), Math.max(from.x, to.x)).mapToObj(x -> new Coordinate(x, from.y)).collect(Collectors.toSet());
        } else {
            throw new IllegalArgumentException("diagonal walls not supported");
        }
    }

    static Collection<Coordinate> wall(String input) {
        Collection<Coordinate> wall = new HashSet<>();
        Coordinate from = null;
        for (String s : input.split(" -> ")) {
            if (from == null) {
                from = Coordinate.from(s);
            } else {
                var to = Coordinate.from(s);
                wall.addAll(wall(from, to));
                from = to;
            }
        }
        return wall;
    }
}

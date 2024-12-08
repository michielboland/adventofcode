package year2024.day8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

public class Day8 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve();
    }
}

class Puzzle {

    final Grid grid;

    Puzzle() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("day8_input"))) {
            grid = Grid.from(new BufferedReader(new InputStreamReader(input)).lines());
        }
    }

    void solve() {
        System.out.println(grid.antinodes().size());
    }
}

record Coordinate(int x, int y) implements Comparable<Coordinate> {
    Coordinate antinode(Coordinate other) {
        return new Coordinate(2 * x() - other.x(), 2 * y() - other.y());
    }

    @Override
    public int compareTo(Coordinate o) {
        return y == o.y ? Integer.compare(x, o.x) : Integer.compare(y, o.y);
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}

record Grid(Map<Integer, SortedSet<Coordinate>> antennas, Map<Coordinate, Integer> coordinates, int width, int height) {

    static Grid from(Stream<String> lines) {
        int width = 0;
        int y = 0;
        Map<Integer, SortedSet<Coordinate>> antennas = new HashMap<>();
        Map<Coordinate, Integer> coordinates = new HashMap<>();
        for (String line : lines.toList()) {
            int x = 0;
            for (int c : line.chars().toArray()) {
                if (c != '.') {
                    var coordinate = new Coordinate(x, y);
                    antennas.computeIfAbsent(c, k -> new TreeSet<>());
                    antennas.get(c).add(coordinate);
                    coordinates.put(coordinate, c);
                }
                x++;
            }
            width = x;
            y++;
        }
        return new Grid(antennas, coordinates, width, y);
    }

    Set<Coordinate> antinodes() {
        Set<Coordinate> antinodes = new TreeSet<>();
        for (Integer i : antennas.keySet()) {
            var set = antennas.get(i);
            for (var a : set) {
                for (var b : set) {
                    if (!a.equals(b)) {
                        var c = a.antinode(b);
                        if (c.x() >= 0 && c.x() < width && c.y() >= 0 && c.y() < height) {
                            antinodes.add(c);
                        }
                    }
                }
            }
        }
        return antinodes;
    }
}

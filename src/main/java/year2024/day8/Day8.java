package year2024.day8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
        System.out.println(grid.antinodes(false).size());
        System.out.println(grid.antinodes(true).size());
    }
}

record Coordinate(int x, int y) {
    Coordinate antinode(Coordinate other) {
        return new Coordinate(2 * x() - other.x(), 2 * y() - other.y());
    }

    Set<Coordinate> antinodes(Coordinate other, int width, int height, boolean resonant) {
        Set<Coordinate> antinodes = new HashSet<>();
        if (resonant) {
            antinodes.add(this);
        }
        var a = this;
        var b = other;
        boolean within;
        do {
            var c = a.antinode(b);
            within = c.x() >= 0 && c.x() < width && c.y() >= 0 && c.y() < height;
            if (within) {
                antinodes.add(c);
            }
            b = a;
            a = c;
        } while (within && resonant);
        return antinodes;
    }
}

record Grid(Map<Integer, Set<Coordinate>> antennas, Map<Coordinate, Integer> coordinates, int width, int height) {

    static Grid from(Stream<String> lines) {
        int width = 0;
        int y = 0;
        Map<Integer, Set<Coordinate>> antennas = new HashMap<>();
        Map<Coordinate, Integer> coordinates = new HashMap<>();
        for (String line : lines.toList()) {
            int x = 0;
            for (int c : line.chars().toArray()) {
                if (c != '.') {
                    var coordinate = new Coordinate(x, y);
                    antennas.computeIfAbsent(c, k -> new HashSet<>());
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

    Set<Coordinate> antinodes(boolean resonant) {
        Set<Coordinate> antinodes = new HashSet<>();
        for (Integer type : antennas.keySet()) {
            var set = antennas.get(type);
            for (var a : set) {
                for (var b : set) {
                    if (!a.equals(b)) {
                        antinodes.addAll(a.antinodes(b, width, height, resonant));
                    }
                }
            }
        }
        return antinodes;
    }
}

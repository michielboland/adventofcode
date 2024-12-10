package year2024.day10;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public class Day10 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve();
    }
}

class Puzzle {
    final Grid grid;

    Puzzle() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("day10_input"))) {
            grid = Grid.from(new BufferedReader(new InputStreamReader(input)).lines());
        }
    }

    void solve() {
        System.out.println(grid.hike());
    }
}

record Coordinate(int x, int y) {
    Collection<Coordinate> neighbours() {
        return Set.of(
                new Coordinate(x + 1, y),
                new Coordinate(x, y + 1),
                new Coordinate(x - 1, y),
                new Coordinate(x, y - 1)
        );
    }
}

record Grid(Map<Coordinate, Integer> heights, Set<Coordinate> trailHeads) {
    static Grid from(Stream<String> lines) {
        int y = 0;
        Map<Coordinate, Integer> heights = new HashMap<>();
        Set<Coordinate> trailHeads = new HashSet<>();
        for (String line : lines.toList()) {
            int x = 0;
            for (int height : line.chars().filter(Character::isDigit).map(i -> i - '0').toArray()) {
                var c = new Coordinate(x, y);
                if (height == 0) {
                    trailHeads.add(c);
                }
                heights.put(c, height);
                ++x;
            }
            ++y;
        }
        return new Grid(heights, trailHeads);
    }

    boolean canHike(Coordinate from, Coordinate to) {
        var i = heights.get(from);
        var j = heights.get(to);
        return j != null && j == i + 1;
    }

    int hike() {
        return trailHeads.stream().mapToInt(this::score).sum();
    }

    int score(Coordinate trailHead) {
        Set<Coordinate> visited = new HashSet<>();
        Set<Coordinate> tops = new HashSet<>();
        Deque<Coordinate> deque = new LinkedList<>();
        deque.addLast(trailHead);
        while (!deque.isEmpty()) {
            var coordinate = deque.removeFirst();
            visited.add(coordinate);
            if (heights.get(coordinate) == 9) {
                tops.add(coordinate);
            }
            for (Coordinate neighbour : coordinate.neighbours()) {
                if (canHike(coordinate, neighbour) && !visited.contains(neighbour)) {
                    deque.addLast(neighbour);
                }
            }
        }
        return tops.size();
    }
}

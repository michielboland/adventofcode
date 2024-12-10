package year2024.day10;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
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

record Pair(int score, int rating) {
    static Pair ZERO() {
        return new Pair(0, 0);
    }
    Pair add(Pair other) {
        return new Pair(score + other.score, rating + other.rating);
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

    Pair hike() {
        return trailHeads.stream().map(this::pair).reduce(Pair.ZERO(), Pair::add);
    }

    Pair pair(Coordinate trailHead) {
        var trails = trails(trailHead);
        int rating = trails.size();
        int score = trails.stream().map(Deque::getLast).collect(Collectors.toSet()).size();
        return new Pair(score, rating);
    }

    Set<Deque<Coordinate>> trails(Coordinate trailHead) {
        Set<Deque<Coordinate>> trails = new HashSet<>();
        Deque<Deque<Coordinate>> deque = new ArrayDeque<>();
        Deque<Coordinate> start = new ArrayDeque<>();
        start.addLast(trailHead);
        deque.addLast(start);
        while (!deque.isEmpty()) {
            var trail = deque.removeFirst();
            Coordinate position = trail.getLast();
            if (heights.get(position) == 9) {
                trails.add(trail);
            } else {
                for (Coordinate neighbour : position.neighbours()) {
                    if (canHike(position, neighbour)) {
                        var xTrail = new ArrayDeque<>(trail);
                        xTrail.addLast(neighbour);
                        deque.addLast(xTrail);
                    }
                }
            }
        }
        return trails;
    }
}

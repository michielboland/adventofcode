package year2024.day12;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

public class Day12 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve();
    }
}

class Puzzle {

    private final Grid grid;

    Puzzle() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("day12_input"))) {
            grid = Grid.from(new BufferedReader(new InputStreamReader(input)).lines());
        }
    }

    void solve() {
        var regions = grid.regions();
        System.out.println(regions.stream().mapToInt(Region::price).sum());
        System.out.println(regions.stream().mapToLong(Region::betterPrice).sum());
    }
}

record Coordinate(int x, int y) implements Comparable<Coordinate> {
    Collection<Coordinate> neighbours() {
        return Set.of(
                new Coordinate(x + 1, y),
                new Coordinate(x, y + 1),
                new Coordinate(x - 1, y),
                new Coordinate(x, y - 1)
        );
    }

    Collection<Coordinate> edges() {
        return Set.of(
                new Coordinate(x, y),
                new Coordinate(x, y + 1),
                new Coordinate(x + 1, y),
                new Coordinate(x + 1, y + 1)
        );
    }

    boolean isNeighbor(Coordinate other) {
        return x == other.x || y == other.y;
    }

    @Override
    public int compareTo(Coordinate o) {
        return y == o.y ? Integer.compare(x, o.x) : Integer.compare(y, o.y);
    }
}

record Region(Map<Coordinate, Integer> plots) {
    int area() {
        return plots.size();
    }

    int perimeter() {
        return plots.values().stream().mapToInt(i -> i).sum();
    }

    int price() {
        return area() * perimeter();
    }

    long edges() {
        Map<Coordinate, SortedSet<Coordinate>> edges = new HashMap<>();
        for (Coordinate coordinate : plots.keySet()) {
            for (Coordinate edge : coordinate.edges()) {
                edges.computeIfAbsent(edge, c -> new TreeSet<>());
                edges.get(edge).add(coordinate);
            }

        }
        long hidden = 2 * edges.values().stream().filter(s -> s.size() == 2 && !s.first().isNeighbor(s.last())).count();
        return edges.values().stream().filter(s -> (s.size() & 1) == 1).count() + hidden;
    }

    long betterPrice() {
        return area() * edges();
    }
}

record Grid(Map<Coordinate, Character> plots) {
    static Grid from(Stream<String> lines) {
        Map<Coordinate, Character> plots = new HashMap<>();
        int y = 0;
        for (String line : lines.toList()) {
            int x = 0;
            for (char c : line.toCharArray()) {
                plots.put(new Coordinate(x, y), c);
                x++;
            }
            y++;
        }
        return new Grid(plots);
    }

    Region region(Coordinate coordinate, Set<Coordinate> visited) {
        var thisPlant = plots.get(coordinate);
        Map<Coordinate, Integer> bits = new HashMap<>();
        Deque<Coordinate> queue = new ArrayDeque<>();
        queue.addLast(coordinate);
        while (!queue.isEmpty()) {
            if (queue.size() > plots.size()) {
                throw new IllegalStateException();
            }
            Coordinate c = queue.removeFirst();
            visited.add(c);
            int fences = 4;
            for (Coordinate neighbour : c.neighbours()) {
                var otherPlant = plots.get(neighbour);
                if (Objects.equals(otherPlant, thisPlant)) {
                    --fences;
                    if (!visited.contains(neighbour)) {
                        if (!queue.contains(neighbour)) {
                            queue.addLast(neighbour);
                        }
                    }
                }
            }
            bits.put(c, fences);
        }
        return new Region(bits);
    }

    List<Region> regions() {
        List<Region> regions = new ArrayList<>();
        Set<Coordinate> visited = new HashSet<>();
        for (Coordinate coordinate : plots.keySet()) {
            if (!visited.contains(coordinate)) {
                regions.add(region(coordinate, visited));
            }
        }
        return regions;
    }
}

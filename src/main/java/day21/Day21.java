package day21;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

enum Heading {
    EAST(Coordinate::east), SOUTH(Coordinate::south), WEST(Coordinate::west), NORTH(Coordinate::north);
    final Function<Coordinate, Coordinate> mover;

    Heading(Function<Coordinate, Coordinate> mover) {
        this.mover = mover;
    }
}

record Coordinate(int x, int y) {
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
}

record Plot(Coordinate coordinate) {
    Set<Coordinate> findNeighbors(Map<Coordinate, Plot> plots) {
        return findNeighbors(64, plots, new HashMap<>()).stream().map(Plot::coordinate).collect(Collectors.toSet());
    }

    private Set<Plot> findNeighbors(int steps, Map<Coordinate, Plot> plots, Map<Key, Set<Plot>> neighbors) {
        Key key = new Key(this, steps);
        if (neighbors.containsKey(key)) {
            return neighbors.get(key);
        }
        Set<Plot> newNeighbors;
        if (steps > 1) {
            var a = 1 << (30 - Integer.numberOfLeadingZeros(steps));
            var b = steps - a;
            newNeighbors = findNeighbors(a, plots, neighbors).stream().flatMap(p -> p.findNeighbors(b, plots, neighbors).stream()).collect(Collectors.toSet());
        } else {
            newNeighbors = Arrays.stream(Heading.values()).map(h -> h.mover.apply(coordinate)).filter(plots::containsKey).map(plots::get).collect(Collectors.toSet());
        }
        neighbors.put(key, newNeighbors);
        return newNeighbors;
    }

    private record Key(Plot plot, Integer value) {
    }
}

record Garden(Map<Coordinate, Plot> plots, Set<Coordinate> rocks, Coordinate startPosition, int width, int height) {
    static Garden parse(Stream<String> lines) {
        var plots = new HashMap<Coordinate, Plot>();
        var rocks = new HashSet<Coordinate>();
        var start = new AtomicReference<Coordinate>();
        var yc = new AtomicInteger();
        var width = lines.mapToInt(l -> {
            var y = yc.incrementAndGet();
            var xc = new AtomicInteger();
            l.chars().forEach(c -> {
                var x = xc.getAndIncrement();
                var coordinate = new Coordinate(x, y);
                switch (c) {
                    case '.', 'S' -> {
                        if (c == 'S') {
                            start.getAndSet(coordinate);
                        }
                        plots.put(coordinate, new Plot(coordinate));
                    }
                    case '#' -> rocks.add(coordinate);
                }
            });
            return xc.get();
        }).max().orElseThrow();
        return new Garden(plots, rocks, start.get(), width, yc.get());
    }

    int solve() {
        return plots.get(startPosition).findNeighbors(plots).size();
    }
}

class Puzzle {
    void solve() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("/day21/day21_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            var garden = Garden.parse(reader.lines());
            System.out.println(garden.solve());
        }
    }
}

public class Day21 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve();
    }
}

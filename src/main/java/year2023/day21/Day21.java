package year2023.day21;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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

record Garden(Set<Coordinate> plots, Set<Coordinate> rocks, Set<Coordinate> reachable, Coordinate startPosition,
              int width, int height) {
    static Garden parse(Stream<String> lines) {
        var plots = new HashSet<Coordinate>();
        var rocks = new HashSet<Coordinate>();
        var start = new AtomicReference<Coordinate>();
        var yc = new AtomicInteger();
        var width = lines.mapToInt(l -> {
            var y = yc.getAndIncrement();
            var xc = new AtomicInteger();
            l.chars().forEach(c -> {
                var x = xc.getAndIncrement();
                var coordinate = new Coordinate(x, y);
                switch (c) {
                    case '.', 'S' -> {
                        if (c == 'S') {
                            start.getAndSet(coordinate);
                        }
                        plots.add(coordinate);
                    }
                    case '#' -> rocks.add(coordinate);
                }
            });
            return xc.get();
        }).max().orElseThrow();
        return new Garden(plots, rocks, new HashSet<>(), start.get(), width, yc.get()).fill();
    }

    static int mod(int a, int b) {
        int r = a % b;
        return r < 0 ? r + b : r;
    }

    long solve(int n) {
        return IntStream.rangeClosed(-n, n)
                .mapToLong(y -> IntStream.rangeClosed(-n, n)
                        .filter(x -> (x + y & 1) == (n & 1))
                        .filter(x -> Math.abs(x) + Math.abs(y) <= n)
                        .filter(x -> reachable(x, y))
                        .count())
                .sum();
    }

    private Coordinate warp(Coordinate coordinate) {
        return new Coordinate(mod(coordinate.x(), width), mod(coordinate.y(), height));
    }

    boolean reachable(int x, int y) {
        var c = warp(new Coordinate(x + startPosition.x(), y + startPosition.y()));
        if (rocks.contains(c)) {
            return false;
        }
        return reachable.contains(c);
    }

    private Garden fill() {
        Deque<Coordinate> deque = new LinkedList<>();
        deque.addLast(startPosition);
        while (!deque.isEmpty()) {
            var coordinate = deque.removeFirst();
            reachable.add(coordinate);
            Set<Coordinate> next = Arrays.stream(Heading.values())
                    .map(h -> h.mover.apply(coordinate))
                    .filter(c -> plots.contains(c) && !reachable.contains(c))
                    .collect(Collectors.toSet());
            reachable.addAll(next);
            deque.addAll(next);
        }
        return this;
    }
}

class Puzzle {
    void solve() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("/year2023/day21/day21_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            var garden = Garden.parse(reader.lines());
            System.out.println(garden.solve(64));
            var width = garden.width();
            var halfWidth = width / 2;
            long[] f = new long[3];
            for (int i = 0; i < 3; i++) {
                var m = halfWidth + 2 * i * width;
                var j = garden.solve(m);
                f[i] = j;
            }
            var a = (f[2] + f[0]) / 2 - f[1];
            var b = (f[2] - f[0]) / 2;
            var c = f[1];
            Function<Long, Long> polynomial = x -> a * x * x + b * x + c;
            System.out.println(polynomial.apply((26501365L - halfWidth) / (2L * width) - 1));
        }
    }
}

public class Day21 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve();
    }
}

package year2025.day7;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class Puzzle {

    private final Grid grid = Grid.parse(new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("day7_input")))).lines());

    public static void main(String[] args) {
        new Puzzle().solve();
    }

    private void solve() {
        System.out.println(part1());
    }

    int part1() {
        return grid.splits();
    }
}

enum Symbol {
    FREE('.'), SPLITTER('^'), START('S');

    final char value;

    Symbol(final char value) {
        this.value = value;
    }

    static Symbol of(char value) {
        return Arrays.stream(values()).filter(s -> s.value == value).findFirst().orElseThrow();
    }
}

record Grid(Set<Coordinate> splitters, Coordinate start, int height) {
    static Grid parse(Stream<String> lines) {
        var starts = new TreeSet<Coordinate>();
        var splitters = new TreeSet<Coordinate>();
        var yc = new AtomicInteger();
        lines.forEach(l -> {
            var y = yc.getAndIncrement();
            var xc = new AtomicInteger();
            l.chars().forEach(c -> {
                var x = xc.getAndIncrement();
                var coordinate = new Coordinate(x, y);
                var symbol = Symbol.of((char) c);
                switch (symbol) {
                    case SPLITTER -> splitters.add(coordinate);
                    case START -> starts.add(coordinate);
                }
            });
        });
        return new Grid(splitters, starts.first(), yc.get());
    }

    int splits() {
        var deque = new ArrayDeque<Coordinate>();
        var visited = new TreeSet<Coordinate>();
        deque.addLast(start);
        visited.add(start);
        int splits = 0;
        while (!deque.isEmpty()) {
            var c = deque.removeFirst();
            var south = c.south();
            if (south.y() < height) {
                if (splitters.contains(south)) {
                    splits++;
                    Stream.of(south.west(), south.east()).forEach(candidate -> {
                        if (!visited.contains(candidate)) {
                            visited.add(candidate);
                            deque.addLast(candidate);
                        }
                    });
                } else {
                    if (!visited.contains(south)) {
                        visited.add(south);
                        deque.addLast(south);
                    }
                }
            }
        }
        return splits;
    }
}

record Coordinate(int p) implements Comparable<Coordinate> {
    static final int M = 1_000;

    public Coordinate(int x, int y) {
        this(x + M * y);
    }

    Coordinate south() {
        return new Coordinate(p + M);
    }

    Coordinate east() {
        return new Coordinate(p + 1);
    }

    Coordinate west() {
        return new Coordinate(p - 1);
    }

    int y() {
        return p / M;
    }

    @Override
    public int compareTo(Coordinate o) {
        return Integer.compare(p, o.p);
    }
}

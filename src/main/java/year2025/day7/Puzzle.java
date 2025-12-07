package year2025.day7;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Objects;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Puzzle {

    private final Grid initialGrid = Grid.parse(new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("day7_input")))).lines());

    public static void main(String[] args) {
        new Puzzle().solve();
    }

    private void solve() {
        System.out.println(part1());
    }

    int part1() {
        var grid = initialGrid;
        do {
            grid = grid.iterate();
        } while (!grid.done());
        return grid.splits();
    }
}

enum Symbol {
    FREE('.'), SPLITTER('^'), START('S'), BEAM('|');

    final char value;

    Symbol(final char value) {
        this.value = value;
    }

    static Symbol of(char value) {
        return Arrays.stream(values()).filter(s -> s.value == value).findFirst().orElseThrow();
    }
}

record Grid(SortedMap<Coordinate, Symbol> map, SortedSet<Coordinate> beamPositions, int splits, int width, int height) {
    static Grid parse(Stream<String> lines) {
        var map = new TreeMap<Coordinate, Symbol>();
        var beamPositions = new TreeSet<Coordinate>();
        var yc = new AtomicInteger();
        var widths = new TreeSet<Integer>();
        lines.forEach(l -> {
            var y = yc.getAndIncrement();
            var xc = new AtomicInteger();
            l.chars().forEach(c -> {
                var x = xc.getAndIncrement();
                var coordinate = new Coordinate(x, y);
                var symbol = Symbol.of((char) c);
                if (symbol == Symbol.START) {
                    beamPositions.add(coordinate);
                }
                map.put(coordinate, symbol);
            });
            widths.add(xc.get());
        });
        return new Grid(map, beamPositions, 0, widths.getFirst(), yc.get());
    }

    boolean done() {
        return beamPositions.isEmpty();
    }

    Grid iterate() {
        var newMap = new TreeMap<>(map);
        var newBeamPositions = new TreeSet<Coordinate>();
        // pass 1 - beam passes straight through
        for (Coordinate beamPosition : beamPositions) {
            var south = beamPosition.south();
            var symbol = newMap.get(south);
            if (symbol == Symbol.FREE) {
                newMap.put(south, Symbol.BEAM);
                newBeamPositions.add(south);
            }
        }
        int newSplits = 0;
        // pass 2 - possible split
        for (Coordinate beamPosition : beamPositions) {
            var south = beamPosition.south();
            var symbol = newMap.get(south);
            if (symbol == Symbol.SPLITTER) {
                newSplits++;
                Stream.of(south.west(), south.east()).forEach(candidate -> {
                    if (newMap.get(candidate) == Symbol.FREE) {
                        newMap.put(candidate, Symbol.BEAM);
                        newBeamPositions.add(candidate);
                    }
                });
            }
        }
        return new Grid(newMap, newBeamPositions, splits + newSplits, width, height);
    }

    @Override
    public String toString() {
        return IntStream.range(0, height).mapToObj(y -> {
            var s = new char[width];
            for (int x = 0; x < width; x++) {
                s[x] = map.get(new Coordinate(x, y)).value;
            }
            return new String(s);
        }).collect(Collectors.joining("\n"));
    }
}

record Coordinate(int p) implements Comparable<Coordinate> {
    static final int M = 1_000;

    public Coordinate(int x, int y) {
        this(x + M * y);
    }

    Coordinate north() {
        return new Coordinate(p - M);
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

    @Override
    public int compareTo(Coordinate o) {
        return Integer.compare(p, o.p);
    }
}

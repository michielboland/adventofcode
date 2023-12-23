package day23;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

enum Heading {
    EAST(Coordinate::east), SOUTH(Coordinate::south), WEST(Coordinate::west), NORTH(Coordinate::north);
    final Mover<Coordinate> mover;

    Heading(Mover<Coordinate> mover) {
        this.mover = mover;
    }

    Set<Heading> next() {
        return switch (this) {
            case NORTH -> Set.of(NORTH, EAST, WEST);
            case SOUTH -> Set.of(SOUTH, EAST, WEST);
            case EAST -> Set.of(EAST, NORTH, SOUTH);
            case WEST -> Set.of(WEST, NORTH, SOUTH);
        };
    }

    static Heading from(int c) {
        return switch (c) {
            case '^' -> NORTH;
            case 'v' -> SOUTH;
            case '<' -> WEST;
            case '>' -> EAST;
            default -> throw new IllegalArgumentException();
        };
    }
}

@FunctionalInterface
interface Mover<T> {
    T move(T movable, long units);
}

record Coordinate(long x, long y) implements Comparable<Coordinate> {
    Coordinate north(long units) {
        return new Coordinate(x, y - units);
    }

    Coordinate south(long units) {
        return new Coordinate(x, y + units);
    }

    Coordinate west(long units) {
        return new Coordinate(x - units, y);
    }

    Coordinate east(long units) {
        return new Coordinate(x + units, y);
    }

    private long[] toArray() {
        return new long[]{y, x};
    }

    @Override
    public int compareTo(Coordinate o) {
        return Arrays.compare(toArray(), o.toArray());
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}

record TrailMap(Set<Coordinate> paths, Map<Coordinate, Heading> slopes, Coordinate start, Coordinate end) {
    static TrailMap parse(Stream<String> lines) {
        var paths = new TreeSet<Coordinate>();
        var slopes = new TreeMap<Coordinate, Heading>();
        var yc = new AtomicLong();
        lines.forEach(l -> {
            var y = yc.getAndIncrement();
            var xc = new AtomicLong();
            l.chars().forEach(ch -> {
                var x = xc.getAndIncrement();
                if (ch != '#') {
                    var c = new Coordinate(x, y);
                    paths.add(c);
                    if (ch != '.') {
                        slopes.put(c, Heading.from(ch));
                    }
                }
            });
        });
        return new TrailMap(paths, slopes, paths.first(), paths.last());
    }

    record CH(Coordinate coordinate, Heading heading, long steps) {
        Stream<CH> next() {
            return heading.next().stream().map(h -> new CH(h.mover.move(coordinate, 1), h, steps + 1L));
        }
    }

    void walk() {
        var queue = new LinkedList<CH>();
        queue.add(new CH(start, Heading.SOUTH, 0L));
        var distances = new TreeSet<Long>();
        while (!queue.isEmpty()) {
            var ch = queue.removeFirst();
            if (ch.coordinate.equals(end)) {
                distances.add(ch.steps);
            }
            queue.addAll(ch.next()
                    .filter(nch -> paths.contains(nch.coordinate))
                    .filter(nch -> !slopes.containsKey(nch.coordinate) || slopes.get(nch.coordinate) == nch.heading)
                    .collect(Collectors.toSet()));
        }
        System.out.println(distances.last());
    }
}

class Puzzle {
    void solve() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("/day23/day23_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            var trailMap = TrailMap.parse(reader.lines());
            trailMap.walk();
        }
    }
}

public class Day23 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve();
    }
}

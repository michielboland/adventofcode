package year2022.day12;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.Function;

public class Puzzle {
    final Coordinate start;
    final Coordinate end;
    final Map<Coordinate, Integer> grid = new HashMap<>();

    Puzzle() throws Exception {
        Coordinate start = null;
        Coordinate end = null;
        try (var reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("day12_input"))))) {
            int y = 0;
            for (String line : reader.lines().toList()) {
                for (int x = 0; x < line.length(); x++) {
                    final var c = line.charAt(x);
                    final int height;
                    var coordinate = new Coordinate(x, y);
                    if (c == 'S') {
                        start = coordinate;
                        height = 0;
                    } else if (c == 'E') {
                        end = coordinate;
                        height = 'z' - 'a';
                    } else {
                        height = c - 'a';
                    }
                    grid.put(coordinate, height);
                }
                ++y;
            }
        }
        this.start = Objects.requireNonNull(start);
        this.end = Objects.requireNonNull(end);
    }

    public static void main(String[] args) throws Exception {
        new Puzzle().solve();
    }

    void solve() {
        System.out.println(shortestDistance());
    }

    int shortestDistance() {
        var queue = new PriorityQueue<ND>();
        Set<CH> visited = new HashSet<>();
        queue.add(new ND(new CH(start, Heading.NOWHERE), 0, null));
        while (!queue.isEmpty()) {
            var current = queue.remove();
            var currentPos = current.ch().coordinate();
            if (end.equals(currentPos)) {
                return current.distance();
            }
            var currentHeight = grid.get(currentPos);
            for (var heading : Heading.headings()) {
                var nextPos = currentPos.move(heading);
                var nextHeight = grid.get(nextPos);
                if (nextHeight != null && nextHeight <= currentHeight + 1) {
                    var ch = new CH(nextPos, heading);
                    if (!visited.contains(ch)) {
                        visited.add(ch);
                        queue.add(new ND(ch, current.distance() + 1, current));
                    }
                }
            }

        }
        throw new IllegalStateException();
    }
}

record ND(CH ch, int distance, ND previous) implements Comparable<ND> {
    @Override
    public int compareTo(ND o) {
        return distance == o.distance ? ch.compareTo(o.ch) : Integer.compare(distance, o.distance);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ND nd = (ND) o;
        return distance == nd.distance && Objects.equals(ch, nd.ch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ch, distance);
    }
}

enum Heading {
    NORTH(Coordinate::north), SOUTH(Coordinate::south), WEST(Coordinate::west), EAST(Coordinate::east), NOWHERE(Function.identity());

    final Function<Coordinate, Coordinate> mover;

    Heading(Function<Coordinate, Coordinate> mover) {
        this.mover = mover;
    }

    static Collection<Heading> headings() {
        return Set.of(NORTH, SOUTH, WEST, EAST);
    }
}

record CH(Coordinate coordinate, Heading heading) implements Comparable<CH> {

    @Override
    public int compareTo(CH o) {
        var comparison = coordinate.compareTo(o.coordinate);
        return comparison == 0 ? heading.compareTo(o.heading) : comparison;
    }
}

record Coordinate(int x, int y) implements Comparable<Coordinate> {
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

    Coordinate move(Heading heading) {
        return heading.mover.apply(this);
    }

    @Override
    public int compareTo(Coordinate o) {
        return y == o.y ? Integer.compare(x, o.x) : Integer.compare(y, o.y);
    }
}

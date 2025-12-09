package year2025.day9;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Puzzle {
    private final List<Coordinate> redTiles;
    private final List<Segment> segments;

    public Puzzle() {
        redTiles = new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("day9_input")))).lines().map(Coordinate::parse).toList();
        segments = new ArrayList<>();
        segments.add(new Segment(redTiles.getLast(), redTiles.getFirst()));
        segments.addAll(IntStream.range(0, redTiles.size() - 1).mapToObj(i -> new Segment(redTiles.get(i), redTiles.get(i + 1))).toList());
    }

    public static void main(String[] args) {
        new Puzzle().solve();
    }

    private void solve() {
        System.out.println(part1());
        // 357176434 is too low
        // 2313828804 is too high
        System.out.println(part2());
    }

    private long part1() {
        return maxArea(true);
    }

    private long part2() {
        return maxArea(false);
    }

    private long maxArea(boolean skipCheck) {
        long maxArea = 0;
        for (int i = 0; i < redTiles.size(); i++) {
            for (int j = i + 1; j < redTiles.size(); j++) {
                var rectangle = new Rectangle(redTiles.get(i), redTiles.get(j));
                var area = rectangle.area();
                if (area > maxArea && (skipCheck || feasible(rectangle))) {
                    maxArea = area;
                }
            }
        }
        return maxArea;
    }

    boolean feasible(Rectangle rectangle) {
        System.err.println(rectangle);
        var sides = rectangle.sides();
        for (Segment side : sides) {
            for (var segment : segments) {
                if (segment.intersects(side)) {
                    return false;
                }
            }
        }
        return true;
    }
}

record Rectangle(Coordinate corner, Coordinate opposite) {
    long area() {
        return (Math.abs(corner.x() - opposite.x()) + 1) * (Math.abs(corner.y() - opposite.y()) + 1);
    }

    List<Segment> sides() {
        var edge1 = new Coordinate(corner.x(), opposite.y());
        var edge2 = new Coordinate(opposite.x(), corner.y());
        return Stream.of(new Segment(corner, edge1), new Segment(edge1, opposite), new Segment(opposite, edge2), new Segment(edge2, corner))
                .filter(Predicate.not(Segment::degenerate)).toList();
    }
}

record Coordinate(long x, long y) {
    static Coordinate parse(String s) {
        var parts = s.split(",");
        return new Coordinate(Long.parseLong(parts[0]), Long.parseLong((parts[1])));
    }

    @Override
    public String toString() {
        return x() + "," + y();
    }
}

record Segment(Coordinate from, Coordinate to) {
    boolean degenerate() {
        return from.equals(to);
    }

    boolean horizontal() {
        return from.y() == to.y();
    }

    Segment {
        if (from.x() != to.x() && from.y() != to.y()) {
            throw new IllegalArgumentException();
        }
    }

    boolean common(Segment other) {
        return from.equals(other.from) || from.equals(other.to) || to.equals(other.from) || to.equals(other.to);
    }

    static boolean between(long x, long a, long b) {
        if (a == b) {
            throw new IllegalArgumentException();
        }
        return x >= Math.min(a, b) && x < Math.max(a, b);
    }

    boolean intersects(Segment other) {
        if (common(other)) {
            return false;
        }
        if (horizontal()) {
            if (other.horizontal()) {
                return from.y() == other.from.y() && (between(from.x(), other.from.x(), other.to.x()) || between(to.x(), other.from.x(), other.to.x()));
            } else {
                return between(other.from.x(), from.x(), to.x()) && between(from.y(), other.from.y(), other.to.y());
            }
        } else {
            if (other.horizontal()) {
                return between(from.x(), other.from.x(), other.to.x()) && between(other.from.y(), from.y(), to.y());
            } else {
                return from.x() == other.from.x() && (between(from.y(), other.from.y(), other.to.y()) || between(to.y(), other.from.y(), other.to.y()));
            }
        }
    }
}

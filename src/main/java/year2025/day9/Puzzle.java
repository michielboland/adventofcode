package year2025.day9;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;

public class Puzzle {
    private final List<Coordinate> redTiles = new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("day9_input")))).lines().map(Coordinate::parse).toList();

    public static void main(String[] args) {
        new Puzzle().solve();
    }

    private void solve() {
        long maxArea = 0;
        for (int i = 0; i < redTiles.size(); i++) {
            for (int j = i + 1; j < redTiles.size(); j++) {
                var rectangle = new Rectangle(redTiles.get(i), redTiles.get(j));
                var area = rectangle.area();
                if (area > maxArea) {
                    System.err.println(rectangle);
                    maxArea = area;
                }
            }
        }
        System.out.println(maxArea);
    }
}

record Rectangle(Coordinate corner, Coordinate opposite) {
    long area() {
        return (Math.abs(corner.x() - opposite.x()) + 1) * (Math.abs(corner.y() - opposite.y()) + 1);
    }
}

record Coordinate(long p) {
    static final long M = 1_000_000L;

    static Coordinate parse(String s) {
        var parts = s.split(",");
        return new Coordinate(Long.parseLong(parts[0]), Long.parseLong(parts[1]));
    }

    Coordinate(long x, long y) {
        this(x + M * y);
    }

    long x() {
        return p % M;
    }

    long y() {
        return p / M;
    }

    Coordinate subtract(Coordinate other) {
        return new Coordinate(p - other.p);
    }

    @Override
    public String toString() {
        return x() + "," + y();
    }
}

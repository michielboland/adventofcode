package day24;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

record Coordinate3(long x, long y, long z) {
    static Coordinate3 from(String s) {
        var a = Arrays.stream(s.split(", +")).mapToLong(Long::valueOf).toArray();
        return new Coordinate3(a[0], a[1], a[2]);
    }

    @Override
    public String toString() {
        return x + ", " + y + ", " + z;
    }
}

record Hailstone(Coordinate3 p, Coordinate3 v) {
    private static final long min = 200000000000000L;
    private static final long max = 400000000000000L;

    static Hailstone from(String s) {
        var a = Arrays.stream(s.split(" @ +")).map(Coordinate3::from).toArray(Coordinate3[]::new);
        return new Hailstone(a[0], a[1]);
    }

    boolean willReach(double x, double y) {
        double dx = x - p.x();
        double dy = y - p.y();
        return switch (dx > 0 ? 1 : dx < 0 ? -1 : 0) {
            case 0 -> switch (dy > 0 ? 1 : dy < 0 ? -1 : 0) {
                case 0 -> false;
                case 1 -> v.y() > 0;
                default -> v.y() < 0;
            };
            case 1 -> v.x() > 0;
            default -> v.x() < 0;
        };
    }

    boolean intersectsXY(Hailstone o) {
        double d = -v.y() * o.v.x() + o.v.y() * v.x();
        if (d != 0) {
            double a = v.y() * p.x() - v.x() * p.y();
            double b = o.v.y() * o.p.x() - o.v.x() * o.p.y();
            double x = (-o.v.x() * a + v.x() * b) / d;
            double y = (-o.v.y() * a + v.y() * b) / d;
            if (willReach(x, y) && o.willReach(x, y)) {
                return x >= min && x <= max && y >= min && y <= max;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return p + " @ " + v;
    }
}

record WeatherMap(List<Hailstone> hailstones) {
    static WeatherMap parse(Stream<String> lines) {
        return new WeatherMap(lines.map(Hailstone::from).toList());
    }

    long solve() {
        return IntStream.range(0, hailstones.size())
                .mapToLong(i -> IntStream.range(i + 1, hailstones.size())
                        .filter(j -> hailstones.get(i).intersectsXY(hailstones.get(j)))
                        .count())
                .sum();
    }
}

class Puzzle {
    void solve() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("/day24/day24_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            var map = WeatherMap.parse(reader.lines());
            System.out.println(map.solve());
        }
    }
}

public class Day24 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve();
    }
}

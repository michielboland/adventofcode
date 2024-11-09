package year2023.day24;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

record Coordinate3(long x, long y, long z) {
    static Coordinate3 from(String s) {
        var a = Arrays.stream(s.split(", +")).mapToLong(Long::valueOf).toArray();
        return new Coordinate3(a[0], a[1], a[2]);
    }

    Coordinate3 add(Coordinate3 other) {
        return new Coordinate3(x + other.x, y + other.y, z + other.z);
    }

    Coordinate3 multiply(long scalar) {
        return new Coordinate3(scalar * x, scalar * y, scalar * z);
    }

    Coordinate3 divide(long scalar) {
        if (x % scalar != 0 || y % scalar != 0 || z % scalar != 0) {
            throw new IllegalArgumentException("division would leave remainder");
        }
        return new Coordinate3(x / scalar, y / scalar, z / scalar);
    }

    @Override
    public String toString() {
        return x + ", " + y + ", " + z;
    }
}

record Slice(long p, long v) {
}

record Hailstone(Coordinate3 p, Coordinate3 v) {
    private static final long min = 200000000000000L;
    private static final long max = 400000000000000L;

    static Hailstone from(String s) {
        var a = Arrays.stream(s.split(" @ +")).map(Coordinate3::from).toArray(Coordinate3[]::new);
        return new Hailstone(a[0], a[1]);
    }

    Hailstone positionAt(long t) {
        return new Hailstone(p.add(v.multiply(t)), v);
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

    Slice x() {
        return new Slice(p.x(), v.x());
    }

    Slice y() {
        return new Slice(p.y(), v.y());
    }

    Slice z() {
        return new Slice(p.z(), v.z());
    }

    @Override
    public String toString() {
        return p + " @ " + v;
    }

    Hailstone merge(Hailstone other) {
        throw new IllegalStateException();
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

    long solve2() {
        List<Function<Hailstone, Slice>> slicers = List.of(Hailstone::x, Hailstone::y, Hailstone::z);
        var collisionRecords = slicers.stream().flatMap(slicer -> hailstones.stream().filter(candidate -> hailstones.stream().filter(h -> !h.equals(candidate)).allMatch(h -> {
            var differenceInPosition = slicer.apply(h).p() - slicer.apply(candidate).p();
            var differenceInVelocity = slicer.apply(h).v() - slicer.apply(candidate).v();
            return differenceInVelocity != 0 && differenceInPosition % differenceInVelocity == 0 && differenceInPosition / differenceInVelocity < 0;
        })).map(candidate ->
                hailstones.stream().filter(h -> !h.equals(candidate)).map(h -> {
                    var collisionTime = (slicer.apply(h).p() - slicer.apply(candidate).p()) / (slicer.apply(candidate).v() - slicer.apply(h).v());
                    return new Collision(collisionTime, h.positionAt(collisionTime));
                }).collect(Collectors.toMap(Collision::when, Collision::hailstone, Hailstone::merge, TreeMap::new)))).toList();
        // somehow we got two results - ignore the second one
        SortedMap<Long, Hailstone> result = collisionRecords.get(0);
        var i = result.entrySet().iterator();
        var first = i.next();
        var second = i.next();
        var firstCollisionTime = first.getKey();
        var firstHailstone = first.getValue();
        var secondCollisionTime = second.getKey();
        var secondHailstone = second.getValue();
        var velocity = secondHailstone.p().add(firstHailstone.p().multiply(-1)).divide(secondCollisionTime - firstCollisionTime);
        var magic = firstHailstone.p().add(velocity.multiply(-firstCollisionTime));
        return magic.x() + magic.y() + magic.z();
    }

    record Collision(Long when, Hailstone hailstone) {
    }
}

class Puzzle {
    void solve() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("/year2023/day24/day24_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            var map = WeatherMap.parse(reader.lines());
            System.out.println(map.solve());
            System.out.println(map.solve2());
        }
    }
}

public class Day24 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve();
    }
}

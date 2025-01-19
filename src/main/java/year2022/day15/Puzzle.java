package year2022.day15;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class Puzzle {
    private static final Pattern PATTERN = Pattern.compile("Sensor at (.*): closest beacon is at (.*)");
    final Set<Coordinate> beacons = new HashSet<>();
    final Set<Sensor> sensors = new HashSet<>();

    Puzzle() throws Exception {
        try (var reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("day15_input"))))) {
            reader.lines().forEach(line -> {
                var matcher = PATTERN.matcher(line);
                if (!matcher.matches()) {
                    throw new IllegalArgumentException(line);
                }
                var beacon = Coordinate.from(matcher.group(2));
                var sensor = Sensor.from(Coordinate.from(matcher.group(1)), beacon);
                beacons.add(beacon);
                sensors.add(sensor);
            });
        }
    }

    public static void main(String[] args) throws Exception {
        new Puzzle().solve();
    }

    SortedSet<ClosedInterval> coverage(int y) {
        return sensors.stream()
                .map(s -> s.coverage(y))
                .filter(Objects::nonNull)
                .<SortedSet<ClosedInterval>>collect(TreeSet::new, ClosedInterval::combine, ClosedInterval::combine);
    }

    int beaconNotPresent(@SuppressWarnings("SameParameterValue") int y) {
        return coverage(y)
                .stream()
                .mapToInt(ClosedInterval::count)
                .sum() - (int) beacons.stream().filter(b -> b.y() == y).count();
    }

    long tuningFrequency() {
        for (int y = 0; y <= 4000000; y++) {
            var coverage = coverage(y);
            switch (coverage.size()) {
                case 1 -> {
                }
                case 2 -> {
                    int x = coverage.first().to() + 1;
                    if (x != coverage.last().from() - 1) {
                        throw new IllegalStateException();
                    }
                    return 4000000L * x + y;
                }
                default -> throw new IllegalStateException();
            }
        }
        return -1;
    }

    void solve() {
        System.out.println(beaconNotPresent(2000000));
        System.out.println(tuningFrequency());
    }
}

record ClosedInterval(int from, int to) implements Comparable<ClosedInterval> {
    boolean overlaps(ClosedInterval other) {
        return to >= other.from && from <= other.to;
    }

    int count() {
        return to - from + 1;
    }

    ClosedInterval combine(ClosedInterval other) {
        return new ClosedInterval(Math.min(from, other.from), Math.max(to, other.to));
    }

    static void combine(SortedSet<ClosedInterval> combined, SortedSet<ClosedInterval> additional) {
        additional.forEach(c -> combine(combined, c));
    }

    static void combine(SortedSet<ClosedInterval> combined, ClosedInterval additional) {
        var it = combined.iterator();
        ClosedInterval replacement = additional;
        while (it.hasNext()) {
            var other = it.next();
            if (replacement.overlaps(other)) {
                it.remove();
                replacement = replacement.combine(other);
            }
        }
        combined.add(replacement);
    }

    @Override
    public int compareTo(ClosedInterval o) {
        return from == o.from ? Integer.compare(to, o.to) : Integer.compare(from, o.from);
    }
}

record Coordinate(int x, int y) {
    private static final Pattern PATTERN = Pattern.compile("x=(.+), y=(.+)");

    static Coordinate from(String s) {
        var matcher = PATTERN.matcher(s);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(s);
        }
        return new Coordinate(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
    }

    int distance(Coordinate other) {
        return Math.abs(x - other.x) + Math.abs(y - other.y);
    }
}

record Sensor(Coordinate position, int beaconDistance) {
    ClosedInterval coverage(int y) {
        int width = beaconDistance - Math.abs(y - position.y());
        return width < 0 ? null : new ClosedInterval(position.x() - width, position.x() + width);
    }

    static Sensor from(Coordinate position, Coordinate beacon) {
        return new Sensor(position, position.distance(beacon));
    }
}

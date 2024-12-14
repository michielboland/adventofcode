package year2024.day14;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class Day14 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve();
    }
}

class Puzzle {
    final List<Robot> robots;

    Puzzle() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("day14_input"))) {
            robots = new BufferedReader(new InputStreamReader(input)).lines().map(Robot::from).toList();
        }
    }

    void solve() {
        Map<Coordinate, Set<Robot>> map = new HashMap<>();
        for (Robot robot : robots) {
            var moved = robot.move();
            var quadrant = moved.quadrant();
            if (quadrant.x() != 0 && quadrant.y() != 0) {
                map.computeIfAbsent(quadrant, k -> new HashSet<>());
                map.get(quadrant).add(moved);
            }
        }
        System.out.println(map.values().stream()
                .mapToLong(Set::size)
                .reduce(1, (a, b) -> a * b));
    }
}

record Coordinate(long x, long y) {
    static Coordinate from(String x, String y) {
        return new Coordinate(Long.parseLong(x), Long.parseLong(y));
    }
}

record Robot(int serial, Coordinate position, Coordinate speed) {
    static final AtomicInteger COUNTER = new AtomicInteger();
    static final Coordinate BOUNDS = new Coordinate(101, 103);
    static final long TIMES = 100;
    static final Pattern PATTERN = Pattern.compile("p=(\\d+),(\\d+) v=(-?\\d+),(-?\\d+)");

    static Robot from(String line) {
        var matcher = PATTERN.matcher(line);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(line);
        }
        return new Robot(
                COUNTER.incrementAndGet(),
                Coordinate.from(matcher.group(1), matcher.group(2)),
                Coordinate.from(matcher.group(3), matcher.group(4))
        );
    }

    static long mod(long a, long b) {
        long c = a % b;
        return c >= 0 ? c : c + b;
    }

    Robot move() {
        return new Robot(serial, new Coordinate(
                mod(position.x() + TIMES * speed.x(), BOUNDS.x()),
                mod(position.y() + TIMES * speed.y(), BOUNDS.y())
        ), speed);
    }

    Coordinate quadrant() {
        return new Coordinate(
                Long.compare(position.x() - BOUNDS.x() / 2, 0),
                Long.compare(position.y() - BOUNDS.y() / 2, 0)
        );
    }
}

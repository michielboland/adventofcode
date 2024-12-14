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
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Day14 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve();
    }
}

class Puzzle {
    final Grid grid;
    final int width = 101;
    final int height = 103;

    Puzzle() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("day14_input"))) {
            grid = Grid.from(new BufferedReader(new InputStreamReader(input)).lines(), width, height);
        }
    }

    void solve() {
        System.out.println(grid.move(100).safetyFactor());
        Grid newGrid = grid;
        for (int i = 0; i <= width * height; i++) {
            if (newGrid.toString().matches("(?s).*\\*{10}.*")) {
                System.out.println(i);
                System.out.println(newGrid);
                break;
            }
            newGrid = newGrid.move(1);
        }
    }
}

record Coordinate(int x, int y) {
    static Coordinate from(String x, String y) {
        return new Coordinate(Integer.parseInt(x), Integer.parseInt(y));
    }
}

record Robot(int serial, Coordinate position, Coordinate speed) {
    static final AtomicInteger COUNTER = new AtomicInteger();
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
}

record Grid(List<Robot> robots, Coordinate bounds) {
    @SuppressWarnings("SameParameterValue")
    static Grid from(Stream<String> lines, int width, int height) {
        return new Grid(lines.map(Robot::from).toList(), new Coordinate(width, height));
    }

    static int mod(int a, int b) {
        int c = a % b;
        return c >= 0 ? c : c + b;
    }

    Robot move(Robot robot, int times) {
        return new Robot(robot.serial(), new Coordinate(
                mod(robot.position().x() + times * robot.speed().x(), bounds.x()),
                mod(robot.position().y() + times * robot.speed().y(), bounds.y())
        ), robot.speed());
    }

    Coordinate quadrant(Robot robot) {
        return new Coordinate(
                Integer.compare(robot.position().x() - bounds.x() / 2, 0),
                Integer.compare(robot.position().y() - bounds.y() / 2, 0)
        );
    }

    Grid move(int times) {
        return new Grid(robots.stream().map(r -> move(r, times)).toList(), bounds);
    }

    long safetyFactor() {
        Map<Coordinate, Set<Robot>> quadrantMap = new HashMap<>();
        for (var robot : robots) {
            var quadrant = quadrant(robot);
            if (quadrant.x() != 0 && quadrant.y() != 0) {
                quadrantMap.computeIfAbsent(quadrant, k -> new HashSet<>());
                quadrantMap.get(quadrant).add(robot);
            }
        }
        return quadrantMap.values().stream()
                .mapToLong(Set::size)
                .reduce(1, (a, b) -> a * b);
    }

    String line(int y) {
        var set = robots.stream().filter(r -> r.position().y() == y)
                .map(r -> r.position().x())
                .collect(Collectors.toSet());
        return IntStream.range(0, bounds.x()).mapToObj(i -> set.contains(i) ? "*" : ".").collect(Collectors.joining());
    }

    @Override
    public String toString() {
        return IntStream.range(0, bounds.y()).mapToObj(this::line).collect(Collectors.joining("\n"));
    }
}

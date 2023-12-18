package day18;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

enum Heading {
    EAST(Coordinate::east), SOUTH(Coordinate::south), WEST(Coordinate::west), NORTH(Coordinate::north);
    final Mover<Coordinate> nextFunction;

    Heading(Mover<Coordinate> nextFunction) {
        this.nextFunction = nextFunction;
    }

    static Heading from(int c) {
        return switch (c) {
            case '0', 'R' -> EAST;
            case '1', 'D' -> SOUTH;
            case '2', 'L' -> WEST;
            case '3', 'U' -> NORTH;
            default -> throw new IllegalArgumentException();
        };
    }
}

enum Turn {
    CLOCKWISE, COUNTERCLOCKWISE;

    static Turn from(Heading from, Heading to) {
        int d = to.ordinal() - from.ordinal() & 0x3;
        return switch (d) {
            case 1 -> CLOCKWISE;
            case 3 -> COUNTERCLOCKWISE;
            default -> throw new IllegalStateException();
        };
    }
}

@FunctionalInterface
interface Mover<T> {
    T move(T movable, long units);
}

public class Day18 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve(false);
        new Puzzle().solve(true);
    }
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

    @Override
    public int compareTo(Coordinate o) {
        return y == o.y ? Long.compare(x, o.x) : Long.compare(y, o.y);
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}

record Instruction(Heading heading, long meters) {
    static final Pattern PATTERN = Pattern.compile("(.) (\\d+) \\(#(.*)(.)\\)");

    static Instruction from(String line, boolean correct) {
        var m = PATTERN.matcher(line);
        if (!m.matches()) {
            throw new IllegalArgumentException();
        }
        if (correct) {
            return new Instruction(Heading.from(m.group(4).codePointAt(0)), Long.parseLong(m.group(3), 16));
        } else {
            return new Instruction(Heading.from(m.group(1).codePointAt(0)), Integer.parseInt(m.group(2)));
        }
    }
}

class Digger {
    Coordinate coordinate = new Coordinate(0, 0);

    Coordinate follow(Instruction instruction) {
        coordinate = instruction.heading().nextFunction.move(coordinate, instruction.meters());
        return coordinate;
    }
}

record DigPlan(List<Instruction> instructions) {
    static DigPlan parse(Stream<String> lines, boolean correct) {
        return new DigPlan(lines.map(line -> Instruction.from(line, correct)).toList());
    }

    static long area(List<Coordinate> path) {
        List<Coordinate> loop = new ArrayList<>(path);
        loop.add(path.get(0));
        long doubleArea = 0;
        //noinspection DuplicatedCode - copied from day 10
        for (int i = 0; i + 1 < loop.size(); i++) {
            var a = loop.get(i);
            var b = loop.get(i + 1);
            doubleArea += a.x() * b.y() - a.y() * b.x();
        }
        return Math.abs(doubleArea) / 2;
    }

    List<Coordinate> coordinates() {
        var digger = new Digger();
        return instructions.stream().map(digger::follow).toList();
    }

    List<Turn> turns() {
        List<Turn> turns = new ArrayList<>();
        var lastHeading = instructions.get(instructions.size() - 1).heading();
        for (Instruction instruction : instructions) {
            var heading = instruction.heading();
            turns.add(Turn.from(lastHeading, heading));
            lastHeading = heading;
        }
        return turns;
    }

    long execute() {
        var map = turns().stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        var totalCounterclockwise = map.getOrDefault(Turn.COUNTERCLOCKWISE, 0L);
        var totalClockwise = map.getOrDefault(Turn.CLOCKWISE, 0L);
        long corners = totalCounterclockwise > totalClockwise ? totalClockwise + 3 * totalCounterclockwise : totalCounterclockwise + 3 * totalClockwise;
        long edges = instructions.stream().mapToLong(i -> i.meters() - 1).sum();
        return area(coordinates()) + (corners + 2 * edges) / 4;
    }
}

class Puzzle {
    void solve(boolean correct) throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("/day18/day18_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            var plan = DigPlan.parse(reader.lines(), correct);
            System.out.println(plan.execute());
        }
    }
}

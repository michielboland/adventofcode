package day18;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

enum Heading {
    NORTH(Coordinate::north), SOUTH(Coordinate::south), EAST(Coordinate::east), WEST(Coordinate::west);
    final Function<Coordinate, Coordinate> nextFunction;

    Heading(Function<Coordinate, Coordinate> nextFunction) {
        this.nextFunction = nextFunction;
    }

    static Heading from(int c) {
        return switch (c) {
            case 'U' -> NORTH;
            case 'D' -> SOUTH;
            case 'L' -> WEST;
            case 'R' -> EAST;
            default -> throw new IllegalArgumentException();
        };
    }
}

public class Day18 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve();
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

    @Override
    public int compareTo(Coordinate o) {
        return y == o.y ? Integer.compare(x, o.x) : Integer.compare(y, o.y);
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}

record Instruction(Heading heading, int meters, Color color) {
    static final Pattern PATTERN = Pattern.compile("(.) (\\d+) \\(#(.*)\\)");

    static Instruction from(String line) {
        var m = PATTERN.matcher(line);
        if (!m.matches()) {
            throw new IllegalArgumentException();
        }
        return new Instruction(Heading.from(m.group(1).codePointAt(0)), Integer.parseInt(m.group(2)), Color.decode("0x" + m.group(3)));
    }
}

class Digger {
    Coordinate coordinate = new Coordinate(0, 0);

    Coordinate head(Heading heading) {
        coordinate = heading.nextFunction.apply(coordinate);
        return coordinate;
    }

    Stream<Coordinate> follow(Instruction instruction) {
        return IntStream.range(0, instruction.meters()).mapToObj(i -> head(instruction.heading()));
    }
}

record DigPlan(List<Instruction> instructions) {
    static DigPlan parse(Stream<String> lines) {
        return new DigPlan(lines.map(Instruction::from).toList());
    }

    List<Coordinate> coordinates() {
        var digger = new Digger();
        return instructions.stream().flatMap(digger::follow).toList();
    }

    // copied from day 10
    static int countInteriorPoints(List<Coordinate> path) {
        List<Coordinate> loop = new ArrayList<>(path);
        loop.add(path.get(0));
        int doubleArea = 0;
        for (int i = 0; i + 1 < loop.size(); i++) {
            var a = loop.get(i);
            var b = loop.get(i + 1);
            doubleArea += a.x() * b.y() - a.y() * b.x();
        }
        return (Math.abs(doubleArea) - path.size()) / 2 + 1;
    }

    int execute() {
        return coordinates().size() + countInteriorPoints(coordinates());
    }
}

class Puzzle {
    void solve() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("/day18/day18_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            var plan = DigPlan.parse(reader.lines());
            System.out.println(plan.execute());
        }
    }
}

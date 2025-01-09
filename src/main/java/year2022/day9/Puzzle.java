package year2022.day9;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public class Puzzle {
    private final List<Instruction> instructions;

    Puzzle() throws Exception {
        try (var reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("day9_input"))))) {
            instructions = reader.lines().map(Instruction::from).toList();
        }
    }

    public static void main(String[] args) throws Exception {
        new Puzzle().solve();
    }

    void solve() {
        System.out.println(withKnots(2));
        // 2447 is too low
        System.out.println(withKnots(10));
    }

    int withKnots(int n) {
        Set<Coordinate> visited = new HashSet<>();
        var rope = Rope.withKnots(n);
        visited.add(rope.tail());
        for (var instruction : instructions) {
            for (int i = 0; i < instruction.steps(); i++) {
                rope = rope.moveHead(instruction.heading());
                visited.add(rope.tail());
            }
        }
        return visited.size();
    }
}

record Coordinate(int x, int y) {
    static final Coordinate START = new Coordinate(0, 0);

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

    Coordinate closest(Coordinate other) {
        return x > other.x + 1 ? west() : x < other.x - 1 ? east() : y > other.y + 1 ? north() : y < other.y - 1 ? south() : other;
    }
}

enum Heading {
    NORTH('U', Coordinate::north), SOUTH('D', Coordinate::south), WEST('L', Coordinate::west), EAST('R', Coordinate::east);

    final char label;
    final Function<Coordinate, Coordinate> mover;

    Heading(char label, Function<Coordinate, Coordinate> mover) {
        this.label = label;
        this.mover = mover;
    }

    static Heading from(char label) {
        return Arrays.stream(values()).filter(h -> h.label == label).findFirst().orElseThrow();
    }
}

record Instruction(Heading heading, int steps) {
    static Instruction from(String line) {
        return new Instruction(Heading.from(line.charAt(0)), Integer.parseInt(line.substring(2)));
    }
}

record Rope(List<Coordinate> knots) {
    static Rope withKnots(int n) {
        List<Coordinate> knots = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            knots.add(Coordinate.START);
        }
        return new Rope(knots);
    }

    Rope moveHead(Heading heading) {
        List<Coordinate> newKnots = new ArrayList<>();
        newKnots.add(knots.get(0).move(heading));
        for (int i = 1; i < knots.size(); i++) {
            newKnots.add(newKnots.get(i - 1).closest(knots.get(i)));
        }
        return new Rope(newKnots);
    }

    Coordinate tail() {
        return knots.get(knots.size() - 1);
    }
}

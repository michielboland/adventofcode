package year2022.day9;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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
        System.out.println(part1());
    }

    int part1() {
        Set<Coordinate> visited = new HashSet<>();
        var rope = new Rope();
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

    boolean isAdjacentTo(Coordinate other) {
        return x >= other.x - 1 && x <= other.x + 1 && y >= other.y - 1 && y <= other.y + 1;
    }
}

enum Heading {
    NORTH('U', Coordinate::north), SOUTH('D', Coordinate::south), WEST('L', Coordinate::west), EAST('R', Coordinate::east), NOWHERE('s', Function.identity());

    final char label;
    final Function<Coordinate, Coordinate> mover;

    Heading(char label, Function<Coordinate, Coordinate> mover) {
        this.label = label;
        this.mover = mover;
    }

    Heading opposite() {
        return switch (this) {
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case EAST -> WEST;
            case WEST -> EAST;
            case NOWHERE -> NOWHERE;
        };
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

record Rope(Coordinate head, Coordinate tail) {
    Rope() {
        this(Coordinate.START, Coordinate.START);
    }

    Rope moveHead(Heading heading) {
        var newHead = head.move(heading);
        var newTail = newHead.isAdjacentTo(tail) ? tail : newHead.move(heading.opposite());
        return new Rope(newHead, newTail);
    }
}

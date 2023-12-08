package day8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

enum Instruction {
    L, R;

    static Instruction from(int i) {
        return switch (i) {
            case 'L' -> L;
            case 'R' -> R;
            default -> throw new IllegalArgumentException();
        };
    }
}

public class Day8 {
    public static void main(String[] args) throws IOException {
        System.out.println(new Puzzle().solve());
    }
}

record Position(String label, String left, String right) {
    private static final Pattern PATTERN = Pattern.compile("(.+) = \\((.+), (.+)\\)");

    static Position from(String input) {
        Matcher matcher = PATTERN.matcher(input);
        if (!matcher.matches()) {
            throw new IllegalArgumentException();
        }
        return new Position(matcher.group(1), matcher.group(2), matcher.group(3));
    }

    String nextLabel(Instruction instruction) {
        return instruction == Instruction.L ? left : right;
    }
}

class DesertMap {
    private final List<Instruction> instructions = new ArrayList<>();
    private final Map<String, Position> positions = new HashMap<>();
    private int ip = 0;

    void readInstructions(String s) {
        instructions.addAll(s.chars().mapToObj(Instruction::from).toList());
    }

    void readPosition(String s) {
        Position position = Position.from(s);
        if (positions.containsKey(position.label())) {
            throw new IllegalStateException();
        }
        positions.put(position.label(), position);
    }

    Position next(Position position) {
        var nextLabel = position.nextLabel(instructions.get(ip));
        ++ip;
        if (ip >= instructions.size()) {
            ip = 0;
        }
        return positions.get(nextLabel);
    }

    int solve() {
        int n = 0;
        final Position from = positions.get("AAA");
        final Position to = positions.get("ZZZ");
        for (Position position = from; position != to; position = next(position)) {
            ++n;
        }
        return n;
    }
}

class Puzzle {
    int solve() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("/day8/day8_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            var map = new DesertMap();
            map.readInstructions(reader.readLine());
            if (!reader.readLine().isBlank()) {
                throw new IllegalStateException();
            }
            reader.lines().forEach(map::readPosition);
            return map.solve();
        }
    }
}

package year2022.day10;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;

public class Puzzle {
    private final List<String> instructions;

    Puzzle() throws Exception {
        try (var reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("day10_input"))))) {
            instructions = reader.lines().toList();
        }
    }

    public static void main(String[] args) throws Exception {
        new Puzzle().solve();
    }

    void solve() {
        var computer = new Computer();
        instructions.forEach(computer::execute);
        System.out.println(computer.totalSignalStrength);
        System.out.println(computer.display());
    }
}

class Computer {
    int cycleCounter;
    int x = 1;
    int totalSignalStrength;
    final StringBuilder output = new StringBuilder();

    void step() {
        var column = cycleCounter % 40;
        output.append(column == x - 1 || column == x || column == x + 1 ? '#' : '.');
        ++cycleCounter;
        if (column == 20) {
            totalSignalStrength += cycleCounter * x;
        }
    }

    void execute(String instruction) {
        var parts = instruction.split(" ");
        step();
        switch (parts[0]) {
            case "noop" -> {
            }
            case "addx" -> {
                step();
                x += Integer.parseInt(parts[1]);
            }
            default -> throw new IllegalArgumentException();
        }
    }

    String display() {
        var sb = new StringBuilder();
        var lines = output.toString();
        for (int i = 0; i < 240; i += 40) {
            sb.append(lines, i, i + 40).append('\n');
        }
        return sb.toString();
    }
}

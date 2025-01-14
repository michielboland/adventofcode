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
        System.out.println(part1());
    }

    int part1() {
        var computer = new Computer();
        instructions.forEach(computer::execute);
        return computer.totalSignalStrength;
    }
}

class Computer {
    int cycleCounter;
    int x = 1;
    int totalSignalStrength;

    void step() {
        ++cycleCounter;
        if (cycleCounter % 40 == 20) {
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
}

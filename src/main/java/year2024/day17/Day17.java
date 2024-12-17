package year2024.day17;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Day17 {
    public static void main(String[] args) throws Exception {
        new Puzzle().solve();
    }
}

class Puzzle {
    final Computer computer;

    Puzzle() throws Exception {
        computer = Computer.from(Files.readString(Paths.get(Objects.requireNonNull(getClass().getResource("day17_input")).toURI())));
    }

    void solve() {
        computer.run();
        System.out.println(computer.output());
    }
}

class Computer {
    final int[] instructions;
    private final List<Integer> output = new ArrayList<>();
    int a;
    int b;
    int c;
    int ip;

    Computer(int a, int b, int c, int[] instructions) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.ip = 0; // redundant
        this.instructions = instructions;
    }

    static Computer from(String program) {
        var parts = program.split("\n\n");
        var matcher = Pattern.compile(".*: (\\d+)\n.*: (\\d+)\n.*: (\\d+)").matcher(parts[0]);
        if (!matcher.matches()) {
            throw new IllegalArgumentException();
        }
        return new Computer(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3)),
                Arrays.stream(parts[1].split(":")[1].replaceAll("\\s", "").split(",")).mapToInt(Integer::parseInt).toArray());
    }

    int combo(int o) {
        return switch (o) {
            case 0, 1, 2, 3 -> o;
            case 4 -> a;
            case 5 -> b;
            case 6 -> c;
            default -> throw new IllegalStateException();
        };
    }

    void run() {
        while (ip < instructions.length) {
            int i = instructions[ip++];
            int o = instructions[ip++];
            switch (i) {
                case 0 -> a = a >> combo(o);
                case 1 -> b = b ^ o;
                case 2 -> b = combo(o) & 7;
                case 3 -> {
                    if (a != 0) {
                        ip = o;
                    }
                }
                case 4 -> b = b ^ c;
                case 5 -> output.add(combo(o) & 7);
                case 6 -> b = a >> combo(o);
                case 7 -> c = a >> combo(o);
            }
        }
    }

    String output() {
        return output.stream().map(String::valueOf).collect(Collectors.joining(","));
    }
}

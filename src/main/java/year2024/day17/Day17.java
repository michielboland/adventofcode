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
        computer = Computer.from(Files.readString(Paths.get(Objects.requireNonNull(getClass().getResource("day17_test")).toURI())));
    }

    void solve() {
        System.out.println(computer);
    }
}

class Computer {
    final int[] instructions;
    final List<Integer> output = new ArrayList<>();
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

    @Override
    public String toString() {
        return """
                Register A: %d
                Register B: %d
                Register C: %d

                Program: %s
                """.formatted(a, b, c,
                Arrays.stream(instructions).boxed().map(String::valueOf).collect(Collectors.joining(",")));
    }
}

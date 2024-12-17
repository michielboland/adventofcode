package year2024.day17;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
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
        System.out.println(computer.run().stream().map(String::valueOf).collect(Collectors.joining(",")));
        System.out.println(computer.selfReplicator());
    }
}

record Computer(int initialA, int initialB, int initialC, int[] instructions) {

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

    long combo(int o, long a, long b, long c) {
        return switch (o) {
            case 0, 1, 2, 3 -> o;
            case 4 -> a;
            case 5 -> b;
            case 6 -> c;
            default -> throw new IllegalStateException();
        };
    }

    List<Integer> run() {
        return run(initialA);
    }

    List<Integer> run(final long newA) {
        List<Integer> output = new ArrayList<>();
        int ip = 0;
        long a = newA;
        long b = initialB;
        long c = initialC;
        while (ip < instructions.length) {
            int i = instructions[ip++];
            int o = instructions[ip++];
            switch (i) {
                case 0 -> a = a >> combo(o, a, b, c);
                case 1 -> b = b ^ o;
                case 2 -> b = combo(o, a, b, c) & 7;
                case 3 -> {
                    if (a != 0) {
                        ip = o;
                    }
                }
                case 4 -> b = b ^ c;
                case 5 -> output.add((int) combo(o, a, b, c) & 7);
                case 6 -> b = a >> combo(o, a, b, c);
                case 7 -> c = a >> combo(o, a, b, c);
            }
        }
        return output;
    }

    long selfReplicator() {
        SortedSet<Long> candidates = new TreeSet<>();
        candidates.add(0L);
        for (int i = 0; i < instructions.length; i++) {
            var lastInstruction = instructions[instructions.length - i - 1];
            SortedSet<Long> newCandidates = new TreeSet<>();
            for (var old : candidates) {
                long candidate = old << 3;
                for (int j = 0; j < 8; j++) {
                    if (run(candidate).get(0) == lastInstruction) {
                        newCandidates.add(candidate);
                    }
                    candidate++;
                }
            }
            candidates = newCandidates;
        }
        return candidates.first();
    }
}

package year2022.day5;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Puzzle {
    final Supplies initialSupplies;
    final Instructions instructions;

    Puzzle() throws Exception {
        var parts = Files.readString(Paths.get(Objects.requireNonNull(getClass().getResource("day5_input")).toURI())).split("\n\n");
        initialSupplies = Supplies.from(parts[0]);
        instructions = Instructions.from(parts[1]);
    }

    public static void main(String[] args) throws Exception {
        new Puzzle().solve();
    }

    void solve() {
        System.out.println(part1());
    }

    String part1() {
        return instructions.apply(initialSupplies).topCrates();
    }
}

record Supplies(Map<Integer, String> stacks) {
    static Supplies from(String string) {
        Map<Integer, String> stacks = new HashMap<>();
        for (String line : string.split("\n")) {
            for (int i = 0; 4 * i + 1 < line.length(); i ++) {
                var c = line.charAt(4 * i + 1);
                if (c >= 'A' && c <= 'Z') {
                    stacks.compute(i + 1, (k, v) -> v == null ? "" + c : v + c);
                }
            }
        }
        return new Supplies(stacks);
    }

    Supplies adjust(Move move) {
        Map<Integer, String> adjustedStacks = new HashMap<>(stacks);
        var stackFrom = adjustedStacks.get(move.from());
        var take = new StringBuilder(stackFrom.substring(0, move.count())).reverse().toString();
        adjustedStacks.put(move.from(), stackFrom.substring(move.count()));
        adjustedStacks.compute(move.to(), (k, v) -> take + v);
        return new Supplies(adjustedStacks);
    }

    String topCrates() {
        return IntStream.range(0, stacks.size()).mapToObj(i -> stacks.get(i + 1).substring(0, 1)).collect(Collectors.joining());
    }
}

record Move(int count, int from, int to) {
    static Move from(String string) {
        var parts = string.split(" ");
        return new Move(Integer.parseInt(parts[1]), Integer.parseInt(parts[3]), Integer.parseInt(parts[5]));
    }
}

record Instructions(List<Move> moves) {
    static Instructions from(String string) {
        return new Instructions(Arrays.stream(string.split("\n")).map(Move::from).toList());
    }

    Supplies apply(final Supplies initial) {
        var supplies = initial;
        for (var move : moves) {
            supplies = supplies.adjust(move);
        }
        return supplies;
    }
}

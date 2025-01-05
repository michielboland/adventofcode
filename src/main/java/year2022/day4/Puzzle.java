package year2022.day4;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;

public class Puzzle {
    private final List<Pair> pairs;

    Puzzle() throws Exception {
        try (var reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("day4_input"))))) {
            pairs = reader.lines().map(Pair::from).toList();
        }
    }

    public static void main(String[] args) throws Exception {
        new Puzzle().solve();
    }

    void solve() {
        System.out.println(part1());
    }

    long part1() {
        return pairs.stream().filter(Pair::contained).count();
    }
}

record Range(int from, int to) {
    static Range from(String input) {
        var parts = input.split("-");
        return new Range(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }

    boolean contains(Range other) {
        return from <= other.from && to >= other.to;
    }
}

record Pair(Range left, Range right) {
    static Pair from(String input) {
        var parts = input.split(",");
        return new Pair(Range.from(parts[0]), Range.from(parts[1]));
    }

    boolean contained() {
        return left.contains(right) || right.contains(left);
    }
}

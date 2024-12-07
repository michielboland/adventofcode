package year2024.day7;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Day7 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve();
    }
}

class Puzzle {
    final List<Equation> equations = new ArrayList<>();

    Puzzle() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("day7_input"))) {
            equations.addAll(new BufferedReader(new InputStreamReader(input)).lines().map(Equation::parse).toList());
        }
    }

    void solve() {
        record Pair(Equation equation, long solutions) {
        }
        long n = equations.stream().map(e -> new Pair(e, e.solutions())).filter(p -> p.solutions > 0).mapToLong(p -> p.equation.solution()).sum();
        System.out.println(n);
    }

}

record Equation(long solution, List<Long> numbers) {
    static Equation parse(String line) {
        var parts = line.split(": ");
        return new Equation(Long.parseLong(parts[0]), Arrays.stream(parts[1].split(" ")).map(Long::parseLong).toList());
    }

    long solutions() {
        return solutions(solution, numbers.size() - 1);
    }

    private long solutions(long solution, int index) {
        if (index == 0) {
            return numbers.get(0) == solution ? 1 : 0;
        } else {
            long last = numbers.get(index);
            long total = 0;
            if (solution > last) {
                total += solutions(solution - last, index - 1);
            }
            if (solution % last == 0) {
                total += solutions(solution / last, index - 1);
            }
            return total;
        }
    }
}

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

    long solutions(boolean hiddenOperator) {
        record Pair(Equation equation, boolean solvable) {
        }
        return equations.stream().map(e -> new Pair(e, e.solvable(hiddenOperator))).filter(Pair::solvable).mapToLong(p -> p.equation.solution()).sum();
    }

    void solve() {
        System.out.println(solutions(false));
        System.out.println(solutions(true));
    }

}

record Equation(long solution, List<Long> numbers) {
    static Equation parse(String line) {
        var parts = line.split(": ");
        return new Equation(Long.parseLong(parts[0]), Arrays.stream(parts[1].split(" ")).map(Long::parseLong).toList());
    }

    boolean solvable(boolean hiddenOperator) {
        return solvable(solution, numbers.size() - 1, hiddenOperator);
    }

    private boolean solvable(long solution, int index, boolean hiddenOperator) {
        if (index == 0) {
            return numbers.get(0) == solution;
        } else {
            long last = numbers.get(index);
            if (solution > last && solvable(solution - last, index - 1, hiddenOperator)) {
                return true;
            }
            if (solution % last == 0 && solvable(solution / last, index - 1, hiddenOperator)) {
                return true;
            }
            if (hiddenOperator) {
                String lastDigits = String.valueOf(last);
                String totalDigits = String.valueOf(solution);
                int remainder = totalDigits.length() - lastDigits.length();
                if (remainder > 0 && totalDigits.endsWith(lastDigits)) {
                    return solvable(Long.parseLong(totalDigits.substring(0, remainder)), index - 1, true);
                }
            }
            return false;
        }
    }
}

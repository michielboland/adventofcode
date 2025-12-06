package year2025.day6;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BinaryOperator;

public class Puzzle {

    private final List<Problem> problems;

    public Puzzle() throws Exception {
        problems = new ArrayList<>();
        var lines = Files.readString(Path.of(Objects.requireNonNull(getClass().getResource("day6_input")).toURI())).split("\n");
        var symbols = lines[lines.length - 1].trim().split(" +");
        for (String symbol : symbols) {
            problems.add(new Problem(new ArrayList<>(), Operation.parse(symbol)));
        }
        for (int i = 0; i < lines.length - 1; i++) {
            var numbers = Arrays.stream(lines[i].trim().split(" +")).map(Long::parseLong).toList();
            for (int j = 0; j < numbers.size(); j++) {
                problems.get(j).numbers().add(numbers.get(j));
            }
        }
    }

    public static void main(String[] args) throws Exception {
        new Puzzle().solve();
    }

    private void solve() {
        System.out.println(part1());
    }

    private long part1() {
        return problems.stream().mapToLong(Problem::answer).sum();
    }
}

record Problem(List<Long> numbers, Operation operation) {
    long answer() {
        return numbers.stream().reduce(operation.neutral, operation.operator);
    }
}

enum Operation {
    ADD('+', 0L, Long::sum), MULTIPLY('*', 1L, (a, b) -> a * b);

    final char symbol;
    final long neutral;
    final BinaryOperator<Long> operator;

    Operation(final char symbol, final long neutral, final BinaryOperator<Long> operator) {
        this.symbol = symbol;
        this.neutral = neutral;
        this.operator = operator;
    }

    static Operation parse(String symbol) {
        return switch (symbol) {
            case "+" -> ADD;
            case "*" -> MULTIPLY;
            default -> throw new IllegalArgumentException();
        };
    }
}

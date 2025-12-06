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
    private final List<Problem> realProblems;

    public Puzzle() throws Exception {
        var lines = Files.readString(Path.of(Objects.requireNonNull(getClass().getResource("day6_input")).toURI())).split("\n");
        problems = parse(lines);
        realProblems = reallyParse(lines);
    }

    static char padAndGet(String s, int index) {
        return index >= s.length() ? ' ' : s.charAt(index);
    }

    static List<Problem> reallyParse(String[] lines) {
        List<Problem> problems = new ArrayList<>();
        int maxLen = Arrays.stream(lines).mapToInt(String::length).max().orElseThrow();
        var lastLine = lines[lines.length - 1];

        for (int i = 0; i < maxLen; i++) {
            var op = padAndGet(lastLine, i);
            switch (op) {
                case '+', '*' -> problems.add(new Problem(new ArrayList<>(), Operation.parse(String.valueOf(op))));
            }
            var digits = new char[lines.length - 1];
            for (int j = 0; j < lines.length - 1; j++) {
                digits[j] = padAndGet(lines[j], i);
            }
            var s = new String(digits).trim();
            if (!s.isEmpty()) {
                problems.getLast().numbers().add(Long.parseLong(s));
            }
        }
        return problems;
    }

    static List<Problem> parse(String[] lines) {
        List<Problem> problems = new ArrayList<>();
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
        return problems;
    }

    public static void main(String[] args) throws Exception {
        new Puzzle().solve();
    }

    private void solve() {
        System.out.println(solve(problems));
        System.out.println(solve(realProblems));
    }

    private static long solve(List<Problem> problems) {
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

package day9;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Day9 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve();
    }
}

record Sequence(int[] numbers) {
    static Sequence from(String input) {
        return new Sequence(Arrays.stream(input.split(" ")).mapToInt(Integer::parseInt).toArray());
    }
    Sequence derived() {
        int[] derived = new int[numbers.length - 1];
        for (int i = 0; i + 1 < numbers.length; i++) {
            derived[i] = numbers[i + 1] - numbers[i];
        }
        return new Sequence(derived);
    }
    boolean isZero() {
        return Arrays.stream(numbers).allMatch(i -> i == 0);
    }
    int next() {
        if (isZero()) {
            return 0;
        }
        return numbers[numbers.length - 1] + derived().next();
    }
}

class Puzzle {
    final List<Sequence> sequences = new ArrayList<>();
    void solve() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("/day9/day9_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            sequences.addAll(reader.lines().map(Sequence::from).toList());
            System.out.println((sequences.stream().mapToInt(Sequence::next).sum()));
        }
    }
}

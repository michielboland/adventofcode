package year2025.day3;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;

public class Puzzle {

    private final List<Bank> banks = new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("day3_input"))))
            .lines()
            .map(Bank::parse)
            .toList();

    public static void main(String[] args) {
        var puzzle = new Puzzle();
        System.out.println(puzzle.joltage(2));
        System.out.println(puzzle.joltage(12));
    }

    long joltage(int picks) {
        return banks.stream().mapToLong(b -> b.joltage(picks)).sum();
    }
}

record Battery(char joltage) {
    static Battery parse(int c) {
        return new Battery((char) c);
    }
}

record Bank(List<Battery> batteries) {
    static Bank parse(String line) {
        return new Bank(line.chars().mapToObj(Battery::parse).toList());
    }

    long joltage(int picks) {
        int start = 0;
        var s = new char[picks];
        for (int i = 0; i < picks; i++) {
            char max = 0;
            for (int p = start; p + picks - i - 1 < batteries.size(); p++) {
                var j = batteries.get(p).joltage();
                if (j > max) {
                    max = j;
                    start = p + 1;
                }
            }
            s[i] = max;
        }
        return Long.parseLong(new String(s));
    }
}

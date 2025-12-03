package year2025.day3;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Puzzle {

    private final List<Bank> banks = new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("day3_input"))))
            .lines()
            .map(Bank::parse)
            .toList();

    public static void main(String[] args) {
        System.out.println(new Puzzle().banks.stream().mapToInt(Bank::joltage).sum());
    }
}

record Battery(int joltage) {
    static Battery parse(int c) {
        return new Battery(c - '0');
    }

    @Override
    public String toString() {
        return String.valueOf(joltage);
    }
}

record Bank(List<Battery> batteries) {
    @Override
    public String toString() {
        return batteries.stream().map(Battery::toString).collect(Collectors.joining());
    }

    static Bank parse(String line) {
        return new Bank(line.chars().mapToObj(Battery::parse).toList());
    }

    int joltage() {
        int l = 0;
        int mp = 0;
        for (int p = 0; p + 1 < batteries.size(); p++) {
            var j = batteries.get(p).joltage();
            if (j > l) {
                l = j;
                mp = p;
            }
        }
        int r = 0;
        for (int p = mp + 1; p < batteries.size(); p++) {
            var j = batteries.get(p).joltage();
            if (j > r) {
                r = j;
            }
        }
        return 10 * l + r;
    }
}

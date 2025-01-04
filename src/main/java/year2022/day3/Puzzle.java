package year2022.day3;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Puzzle {
    private final List<String> input;

    Puzzle() throws Exception {
        input = Arrays.stream(Files.readString(Paths.get(Objects.requireNonNull(getClass().getResource("day3_input")).toURI())).split("\n")).toList();
    }

    public static void main(String[] args) throws Exception {
        new Puzzle().solve();
    }

    void solve() {
        System.out.println(part1());
    }

    int part1() {
        return input.stream().mapToInt(this::priority).sum();
    }

    int priority(int c) {
        if (c >= 'a' && c <= 'z') {
            return c - 'a' + 1;
        } else if (c >= 'A' && c <= 'Z') {
            return c - 'A' + 27;
        }
        throw new IllegalArgumentException();
    }

    int priority(String line) {
        var left = line.substring(0, line.length() >> 1);
        var right = line.substring(line.length() >> 1);
        var types = left.chars().boxed().collect(Collectors.toSet());
        types.retainAll(right.chars().boxed().collect(Collectors.toSet()));
        return priority(types.iterator().next());
    }
}

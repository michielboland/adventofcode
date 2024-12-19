package year2024.day19;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Day19 {
    public static void main(String[] args) throws Exception {
        new Puzzle().solve();
    }
}

class Puzzle {
    final Set<String> towels;
    final List<String> patterns;
    final int maxLen;

    Puzzle() throws Exception {
        var input = Files.readString(Paths.get(Objects.requireNonNull(getClass().getResource("day19_input")).toURI()));
        var parts = input.split("\n\n");
        towels = Arrays.stream(parts[0].split(", ")).collect(Collectors.toSet());
        patterns = Arrays.stream(parts[1].split("\n")).toList();
        maxLen = towels.stream().mapToInt(String::length).max().orElseThrow();
    }

    long solutions(String s) {
        return solutions(s, 0, s.length());
    }

    long solutions(String pattern, int from, int to) {
        var portion = pattern.substring(from, to);
        if (portion.isEmpty()) {
            return 1;
        }
        if (towels.contains(portion)) {
            return 1;
        }
        if (to == from + 1) {
            return 0;
        }
        int middle = (from + to) / 2;
        long total = solutions(pattern, from, middle) * solutions(pattern, middle, to);
        for (int towelSize = 2; towelSize <= maxLen; towelSize++) {
            for (int offset = middle - towelSize + 1; offset < middle; offset++) {
                if (offset < from || offset + towelSize > to) {
                    continue;
                }
                if (towels.contains(pattern.substring(offset, offset + towelSize))) {
                    total += solutions(pattern, from, offset) * solutions(pattern, offset + towelSize, to);
                }
            }
        }
        return total;
    }

    void solve() {
        // 76514031378 is too low
        System.out.println(patterns.stream().filter(p -> solutions(p) > 0).count());
        System.out.println(patterns.stream().mapToLong(this::solutions).sum());
    }
}

package year2024.day19;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    Map<String, Long> solutions = new HashMap<>();

    Puzzle() throws Exception {
        var input = Files.readString(Paths.get(Objects.requireNonNull(getClass().getResource("day19_input")).toURI()));
        var parts = input.split("\n\n");
        towels = Arrays.stream(parts[0].split(", ")).collect(Collectors.toSet());
        patterns = Arrays.stream(parts[1].split("\n")).toList();
        maxLen = towels.stream().mapToInt(String::length).max().orElseThrow();
    }

    long solutions(String pattern) {
        if (pattern.isEmpty()) {
            return 1;
        }
        if (solutions.containsKey(pattern)) {
            return solutions.get(pattern);
        }
        long total = 0;
        for (int i = 1; i <= maxLen && i <= pattern.length(); i++) {
            if (towels.contains(pattern.substring(0, i))) {
                total += solutions(pattern.substring(i));
            }
        }
        solutions.put(pattern, total);
        return total;
    }

    void solve() {
        System.out.println(patterns.stream().filter(p -> solutions(p) > 0).count());
        System.out.println(patterns.stream().mapToLong(this::solutions).sum());
    }
}
